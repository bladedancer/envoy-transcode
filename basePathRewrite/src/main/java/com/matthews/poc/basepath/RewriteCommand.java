package com.matthews.poc.basepath;

import com.google.api.CustomHttpPattern;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "rewrite",
        description = "Writes the url in the protobuf descriptor",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class RewriteCommand extends BaseCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-b", "--basepath"}, description = "The basepath", required = true)
    String basepath;

    @CommandLine.Option(names = {"-p", "--package"}, description = "Protobuf package", required = true)
    String packageName;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Output file", defaultValue = "updated.pb")
    String outputFile;

    @Override
    public Integer call() throws Exception {
        try {
            log.info("Starting protobuf descriptor rewrite for package: {} with basepath: {}", packageName, basepath);
            rewriteProtobufDescriptors();
            log.info("Successfully wrote updated protobuf descriptor to: {}", outputFile);
            return 0;
        } catch (Exception e) {
            log.error("Error during protobuf rewrite: {}", e.getMessage(), e);
            return 1;
        }
    }

    /**
     * Rewrites HTTP paths in protobuf descriptors by prepending the base path
     */
    public void rewriteProtobufDescriptors() throws IOException, Descriptors.DescriptorValidationException {
        // Load all descriptors from the original file
        java.util.List<Descriptors.FileDescriptor> originalDescriptors = loadAllProtobufDescriptors();

        // Create a new descriptor set with rewritten HTTP paths
        DescriptorProtos.FileDescriptorSet.Builder descriptorSetBuilder = DescriptorProtos.FileDescriptorSet.newBuilder();

        for (Descriptors.FileDescriptor fileDescriptor : originalDescriptors) {
            DescriptorProtos.FileDescriptorProto.Builder fileProtoBuilder = fileDescriptor.toProto().toBuilder();

            // Only rewrite services in the specified package
            if (packageName.equals(fileDescriptor.getPackage())) {
                log.info("Rewriting HTTP paths for package: {}", fileDescriptor.getPackage());
                rewriteServicesInFile(fileProtoBuilder);
            }

            descriptorSetBuilder.addFile(fileProtoBuilder.build());
        }

        // Write the updated descriptor set to the output file
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            descriptorSetBuilder.build().writeTo(fos);
            log.info("Written updated protobuf descriptor to: {}", outputFile);
        }
    }

    /**
     * Loads all protobuf descriptors from the file (no package filtering)
     */
    private java.util.List<Descriptors.FileDescriptor> loadAllProtobufDescriptors() throws IOException, Descriptors.DescriptorValidationException {
        return loadProtobufDescriptor(null); // Pass null to load all packages
    }

    /**
     * Rewrites HTTP paths in all services within a file descriptor
     */
    private void rewriteServicesInFile(DescriptorProtos.FileDescriptorProto.Builder fileProtoBuilder) {
        for (int serviceIndex = 0; serviceIndex < fileProtoBuilder.getServiceCount(); serviceIndex++) {
            DescriptorProtos.ServiceDescriptorProto.Builder serviceBuilder = fileProtoBuilder.getServiceBuilder(serviceIndex);

            log.info("Processing service: {}", serviceBuilder.getName());

            for (int methodIndex = 0; methodIndex < serviceBuilder.getMethodCount(); methodIndex++) {
                DescriptorProtos.MethodDescriptorProto.Builder methodBuilder = serviceBuilder.getMethodBuilder(methodIndex);

                if (methodBuilder.hasOptions() && methodBuilder.getOptions().hasExtension(AnnotationsProto.http)) {
                    HttpRule originalRule = methodBuilder.getOptions().getExtension(AnnotationsProto.http);
                    HttpRule rewrittenRule = rewriteHttpRule(originalRule);

                    // Update the method options with the rewritten HTTP rule
                    DescriptorProtos.MethodOptions.Builder optionsBuilder = methodBuilder.getOptionsBuilder();
                    optionsBuilder.setExtension(AnnotationsProto.http, rewrittenRule);

                    log.info("Rewritten HTTP path for method {}: {} -> {}",
                        methodBuilder.getName(),
                        getHttpPath(originalRule),
                        getHttpPath(rewrittenRule));
                }
            }
        }
    }

    /**
     * Rewrites an HTTP rule by prepending the base path to the URL
     */
    private HttpRule rewriteHttpRule(HttpRule originalRule) {
        HttpRule.Builder ruleBuilder = originalRule.toBuilder();

        // Rewrite the main HTTP rule
        String newPath = prependBasePath(getHttpPath(originalRule));
        setHttpPath(ruleBuilder, originalRule.getPatternCase(), newPath);

        // Rewrite additional bindings if any
        for (int i = 0; i < ruleBuilder.getAdditionalBindingsCount(); i++) {
            HttpRule.Builder bindingBuilder = ruleBuilder.getAdditionalBindingsBuilder(i);
            HttpRule originalBinding = ruleBuilder.getAdditionalBindings(i);
            String newBindingPath = prependBasePath(getHttpPath(originalBinding));
            setHttpPath(bindingBuilder, originalBinding.getPatternCase(), newBindingPath);
        }

        return ruleBuilder.build();
    }

    /**
     * Sets the HTTP path on an HttpRule builder based on the pattern case
     */
    private void setHttpPath(HttpRule.Builder ruleBuilder, HttpRule.PatternCase patternCase, String newPath) {
        switch (patternCase) {
            case GET -> ruleBuilder.setGet(newPath);
            case PUT -> ruleBuilder.setPut(newPath);
            case POST -> ruleBuilder.setPost(newPath);
            case DELETE -> ruleBuilder.setDelete(newPath);
            case PATCH -> ruleBuilder.setPatch(newPath);
            case CUSTOM -> {
                CustomHttpPattern.Builder customBuilder = ruleBuilder.getCustomBuilder();
                customBuilder.setPath(newPath);
            }
        }
    }

    /**
     * Prepends the base path to a URL path, handling proper path concatenation
     */
    private String prependBasePath(String originalPath) {
        if (originalPath == null || originalPath.isEmpty()) {
            return basepath;
        }

        // Ensure base path starts with / and doesn't end with /
        String cleanBasePath = basepath;
        if (!cleanBasePath.startsWith("/")) {
            cleanBasePath = "/" + cleanBasePath;
        }
        if (cleanBasePath.endsWith("/") && cleanBasePath.length() > 1) {
            cleanBasePath = cleanBasePath.substring(0, cleanBasePath.length() - 1);
        }

        // Ensure original path starts with /
        String cleanOriginalPath = originalPath;
        if (!cleanOriginalPath.startsWith("/")) {
            cleanOriginalPath = "/" + cleanOriginalPath;
        }

        return cleanBasePath + cleanOriginalPath;
    }
}
