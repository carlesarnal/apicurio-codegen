package io.quarkus.apicurio.codegen.deployment.wrapper;

import io.quarkus.apicurio.codegen.deployment.OpenApiServerGeneratorWrapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OpenApiServerGeneratorWrapperTest {

    @Test
    void generatePetStore() throws URISyntaxException, IOException {

        final String petstoreOpenApi = this.getClass().getResource("/openapi/beer-api.json").getPath();
        final String targetPath = Paths.get(getClass().getResource("/").toURI()).getParent().toString() + "/openapi-gen";
        final OpenApiServerGeneratorWrapper generatorWrapper = new OpenApiServerGeneratorWrapper(petstoreOpenApi);
        final ByteArrayOutputStream generate = generatorWrapper.generate();


        // Validate the result
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(generate.toByteArray()))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.isDirectory()) {
                    String name = zipEntry.getName();

                    assertNotNull(name);

                    URL expectedFile = getClass().getClassLoader().getResource(getClass().getSimpleName() + "/" + targetPath + "/" + name);
                    if (expectedFile == null && "PROJECT_GENERATION_FAILED.txt".equals(name)) {
                        String errorLog = IOUtils.toString(zipInputStream, StandardCharsets.UTF_8);
                        System.out.println("----- UNEXPECTED ERROR LOG -----");
                        System.out.println(errorLog);
                        System.out.println("----- UNEXPECTED ERROR LOG -----");
                    }
                    String expected = IOUtils.toString(expectedFile, StandardCharsets.UTF_8);

                    String actual = IOUtils.toString(zipInputStream, StandardCharsets.UTF_8);

                    assertEquals("Expected vs. actual failed for entry: " + name, normalizeString(expected), normalizeString(actual));
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private static String normalizeString(String value) {
        value = value.replaceAll("\\r\\n", "\n");
        value = value.replaceAll("\\r", "\n");
        return value;
    }
}
