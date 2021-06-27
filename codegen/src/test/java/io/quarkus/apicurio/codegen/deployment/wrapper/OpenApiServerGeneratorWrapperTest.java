package io.quarkus.apicurio.codegen.deployment.wrapper;

import io.quarkus.apicurio.codegen.deployment.OpenApiServerGeneratorWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OpenApiServerGeneratorWrapperTest {

    @Test
    void generateServerClasses() throws IOException {

        final String openApiDefinition = this.getClass().getResource("/OpenApiServerGeneratorWrapperTest/beer-api.json").getPath();
        final OpenApiServerGeneratorWrapper generatorWrapper = new OpenApiServerGeneratorWrapper(openApiDefinition);
        final ByteArrayOutputStream generate = generatorWrapper.generate();

        File tempFile = File.createTempFile("api", ".zip");
        FileUtils.writeByteArrayToFile(tempFile, generate.toByteArray());
        System.out.println("Generated ZIP (debug) can be found here: " + tempFile.getAbsolutePath());

        // Validate the result
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(generate.toByteArray()))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.isDirectory()) {
                    String name = zipEntry.getName();
                    System.out.println(name);
                    Assertions.assertNotNull(name);

                    String expectedFilesPath = "/generated-api";
                    URL expectedFile = getClass().getClassLoader().getResource(getClass().getSimpleName() + "/" + expectedFilesPath + "/" + name);
                    if (expectedFile == null && "PROJECT_GENERATION_FAILED.txt".equals(name)) {
                        String errorLog = IOUtils.toString(zipInputStream, StandardCharsets.UTF_8);
                        System.out.println("----- UNEXPECTED ERROR LOG -----");
                        System.out.println(errorLog);
                        System.out.println("----- UNEXPECTED ERROR LOG -----");
                    }
                    Assertions.assertNotNull(name);
                    String expected = IOUtils.toString(expectedFile, StandardCharsets.UTF_8);

                    String actual = IOUtils.toString(zipInputStream, StandardCharsets.UTF_8);
                    System.out.println("-----");
                    System.out.println(actual);
                    System.out.println("-----");
                    Assertions.assertEquals(normalizeString(expected), normalizeString(actual));
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
