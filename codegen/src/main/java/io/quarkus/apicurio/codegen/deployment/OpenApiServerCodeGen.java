package io.quarkus.apicurio.codegen.deployment;

import io.quarkus.bootstrap.prebuild.CodeGenException;
import io.quarkus.deployment.CodeGenContext;
import io.quarkus.deployment.CodeGenProvider;
import io.quarkus.utilities.OS;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;

public class OpenApiServerCodeGen implements CodeGenProvider {

    private static final Logger log = Logger.getLogger(OpenApiServerCodeGen.class);

    private static final String JSON = ".json";
    private static final String YML = ".yml";
    private static final String YAML = ".yaml";

    @Override
    public String providerId() {
        return "openapi-server-generator";
    }

    @Override
    public String inputExtension() {
        return "json";
    }

    @Override
    public String inputDirectory() {
        return "openapi";
    }

    @Override
    public boolean trigger(CodeGenContext context) throws CodeGenException {
        if (TRUE.toString().equalsIgnoreCase(System.getProperties().getProperty("openapi.codegen.skip", "false"))) {
            log.info("Skipping " + this.getClass() + " invocation on user's request");
            return false;
        }

        final Path outDir = context.outDir();
        final Path openApiDir = context.inputDir();
        try {
            if (Files.isDirectory(openApiDir)) {
                try (Stream<Path> openApiFilesPaths = Files.walk(openApiDir)) {
                    final List<String> openApiFiles = openApiFilesPaths
                            .filter(Files::isRegularFile)
                            .map(Path::toString)
                            .filter(s -> s.endsWith(YAML) || s.endsWith(YML) || s.endsWith(JSON))
                            .map(this::escapeWhitespace)
                            .collect(Collectors.toList());
                    for (String openApiFile : openApiFiles) {
                        final OpenApiServerGeneratorWrapper generator = new OpenApiServerGeneratorWrapper(openApiFile,
                                outDir.toString());
                        generator.generate();
                    }
                    return true;
                }
            }
        } catch (IOException e) {
            throw new CodeGenException("Failed to create java project from openapi definition", e);
        }
        return false;
    }

    private String escapeWhitespace(String path) {
        if (OS.determineOS() == OS.LINUX) {
            return path.replace(" ", "\\ ");
        } else {
            return path;
        }
    }
}
