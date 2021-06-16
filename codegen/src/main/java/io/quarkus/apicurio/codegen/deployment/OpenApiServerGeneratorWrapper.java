package io.quarkus.apicurio.codegen.deployment;

import io.apicurio.hub.api.codegen.OpenApi2JaxRs;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class OpenApiServerGeneratorWrapper {

    private final OpenApi2JaxRs generator;
    private final String outputDir;

    public OpenApiServerGeneratorWrapper(final String specFilePath, final String outputDir) throws IOException {
        OpenApi2JaxRs.JaxRsProjectSettings settings = new OpenApi2JaxRs.JaxRsProjectSettings();
        settings.codeOnly = false;
        settings.artifactId = "generated-api";
        settings.groupId = "org.example.api";
        settings.javaPackage = "org.example.api";

        OpenApi2JaxRs generator = new OpenApi2JaxRs();
        generator.setSettings(settings);
        generator.setOpenApiDocument(getClass().getClassLoader().getResource(specFilePath));

        this.generator = generator;
        this.outputDir = outputDir;
    }

    public void generate() throws IOException {
        final ByteArrayOutputStream generate = generator.generate();

        File tempFile = new File(outputDir);
        FileUtils.writeByteArrayToFile(tempFile, generate.toByteArray());
    }
}
