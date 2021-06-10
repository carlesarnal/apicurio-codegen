package io.quarkus.apicurio.codegen.deployment;

import io.apicurio.hub.api.codegen.OpenApi2JaxRs;
import io.quarkus.bootstrap.prebuild.CodeGenException;
import io.quarkus.deployment.CodeGenContext;
import io.quarkus.deployment.CodeGenProvider;
import io.quarkus.utilities.OS;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;

public class OpenApiCodeGenProvider implements CodeGenProvider {

    private static final Logger log = Logger.getLogger(OpenApiCodeGenProvider.class);

    private static final String OPEN_API = ".json";

    @Override
    public String providerId() {
        return "openapi";
    }

    @Override
    public String inputExtension() {
        return "json";
    }

    @Override
    public String inputDirectory() {
        return "resources";
    }

    @Override
    public boolean trigger(CodeGenContext context) throws CodeGenException {
        if (TRUE.toString().equalsIgnoreCase(System.getProperties().getProperty("openapi.codegen.skip", "false"))) {
            log.info("Skipping " + this.getClass() + " invocation on user's request");
            return false;
        }

        Path outDir = context.outDir();
        Path workDir = context.workDir();
        Path inputDir = context.inputDir();

        OpenApi2JaxRs.JaxRsProjectSettings settings = new OpenApi2JaxRs.JaxRsProjectSettings();
        settings.codeOnly = false;
        settings.artifactId = "generated-api";
        settings.groupId = "org.example.api";
        settings.javaPackage = "org.example.api";

        try {
            if (Files.isDirectory(inputDir)) {
                try (Stream<Path> protoFilesPaths = Files.walk(inputDir)) {
                    String openApiDefs = protoFilesPaths
                            .filter(Files::isRegularFile)
                            .map(Path::toString)
                            .filter(s -> s.endsWith(OPEN_API))
                            .map(this::escapeWhitespace)
                            .findFirst()
                            .orElseThrow(() -> new CodeGenException("Failed to create java project from openapi definition, no definition found"));

                    OpenApi2JaxRs generator = new OpenApi2JaxRs();
                    generator.setSettings(settings);
                    generator.setOpenApiDocument(getClass().getClassLoader().getResource(openApiDefs));
                    ByteArrayOutputStream outputStream = null;
                    outputStream = generator.generate();
                    File tempFile = File.createTempFile("api", ".zip");
                    FileUtils.writeByteArrayToFile(tempFile, outputStream.toByteArray());
                }
            }
            return true;
        } catch (IOException e) {
            throw new CodeGenException("Failed to create java project from openapi definition", e);
        }
    }

    private String escapeWhitespace(String path) {
        if (OS.determineOS() == OS.LINUX) {
            return path.replace(" ", "\\ ");
        } else {
            return path;
        }
    }
}
