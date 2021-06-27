package io.quarkus.apicurio.codegen.deployment;

import io.apicurio.hub.api.codegen.OpenApi2JaxRs;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class OpenApiServerGeneratorWrapper {

    private final OpenApi2JaxRs generator;

    public OpenApiServerGeneratorWrapper(final String specFilePath) throws IOException {
        OpenApi2JaxRs.JaxRsProjectSettings settings = new OpenApi2JaxRs.JaxRsProjectSettings();
        settings.codeOnly = false;
        settings.artifactId = "generated-api";
        settings.groupId = "org.example.api";
        settings.javaPackage = "org.example.api";

        generator = new OpenApi2JaxRs();
        generator.setSettings(settings);
        generator.setOpenApiDocument(new FileInputStream(specFilePath));
    }

    public ByteArrayOutputStream generate() throws IOException {
        return generator.generate();
    }
}
