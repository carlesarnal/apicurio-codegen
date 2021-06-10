package io.quarkus.apicurio.codegen.deployment;

import io.quarkus.bootstrap.prebuild.CodeGenException;
import io.quarkus.deployment.CodeGenContext;
import io.quarkus.deployment.CodeGenProvider;
import org.jboss.logging.Logger;

import java.nio.file.Path;

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
        Path protoDir = context.inputDir();
        return false;
    }
}
