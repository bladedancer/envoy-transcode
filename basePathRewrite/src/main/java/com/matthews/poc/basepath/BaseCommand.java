package com.matthews.poc.basepath;

import picocli.CommandLine;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.api.AnnotationsProto;

import java.io.FileInputStream;
import java.io.IOException;

public class BaseCommand {
    @CommandLine.Option(names = {"-f", "--file"}, description = "Protobuf descriptor file", required = true)
    String proto;

    /**
     * Loads all protobuf descriptors for the specified package from the file
     * @param packageName the package name to filter descriptors by
     * @return List of FileDescriptors for the specified package
     * @throws IOException if the file cannot be read
     * @throws Descriptors.DescriptorValidationException if any descriptor is invalid
     */
    public java.util.List<Descriptors.FileDescriptor> loadProtobufDescriptor(String packageName) throws IOException, Descriptors.DescriptorValidationException {
        try (FileInputStream fis = new FileInputStream(proto)) {
            // Create extension registry to handle google.api.http annotations
            ExtensionRegistry registry = ExtensionRegistry.newInstance();
            AnnotationsProto.registerAllExtensions(registry);

            // Parse the descriptor set from the file
            DescriptorProtos.FileDescriptorSet descriptorSet =
                DescriptorProtos.FileDescriptorSet.parseFrom(fis, registry);

            // Build a map to store all file descriptors by name for dependency resolution
            java.util.Map<String, Descriptors.FileDescriptor> builtDescriptors = new java.util.HashMap<>();
            java.util.List<Descriptors.FileDescriptor> resultDescriptors = new java.util.ArrayList<>();

            // First pass: collect all proto files and sort by dependencies
            java.util.List<DescriptorProtos.FileDescriptorProto> allProtos = new java.util.ArrayList<>(descriptorSet.getFileList());

            // Keep building descriptors until all are processed
            while (!allProtos.isEmpty()) {
                boolean madeProgress = false;
                java.util.Iterator<DescriptorProtos.FileDescriptorProto> iterator = allProtos.iterator();

                while (iterator.hasNext()) {
                    DescriptorProtos.FileDescriptorProto fileProto = iterator.next();

                    // Check if all dependencies are already built
                    boolean canBuild = true;
                    java.util.List<Descriptors.FileDescriptor> dependencies = new java.util.ArrayList<>();

                    for (String dependencyName : fileProto.getDependencyList()) {
                        Descriptors.FileDescriptor dependency = builtDescriptors.get(dependencyName);
                        if (dependency == null) {
                            // Try to get from well-known types
                            dependency = getWellKnownTypeDescriptor(dependencyName);
                        }

                        if (dependency != null) {
                            dependencies.add(dependency);
                        } else {
                            canBuild = false;
                            break;
                        }
                    }

                    if (canBuild) {
                        try {
                            Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(
                                fileProto,
                                dependencies.toArray(new Descriptors.FileDescriptor[0])
                            );

                            builtDescriptors.put(fileProto.getName(), fileDescriptor);

                            // Add to result if it matches the package filter
                            if (packageName == null || packageName.isEmpty() || fileProto.getPackage().equals(packageName)) {
                                resultDescriptors.add(fileDescriptor);
                            }

                            iterator.remove();
                            madeProgress = true;
                        } catch (Descriptors.DescriptorValidationException e) {
                            // Skip this iteration, might be resolved later
                            continue;
                        }
                    }
                }

                if (!madeProgress) {
                    // If we can't make progress, there might be unresolvable dependencies
                    throw new RuntimeException(
                        "Cannot resolve dependencies for remaining proto files: " +
                        allProtos.stream()
                            .map(DescriptorProtos.FileDescriptorProto::getName)
                            .collect(java.util.stream.Collectors.joining(", ")));
                }
            }

            if (resultDescriptors.isEmpty()) {
                throw new IllegalArgumentException("No file descriptors found for package: " + (packageName != null ? packageName : "any"));
            }

            return resultDescriptors;
        }
    }

    /**
     * Gets well-known type descriptors for common Google protobuf types
     */
    private Descriptors.FileDescriptor getWellKnownTypeDescriptor(String fileName) {
        return switch (fileName) {
            case "google/protobuf/timestamp.proto" -> com.google.protobuf.TimestampProto.getDescriptor();
            case "google/protobuf/duration.proto" -> com.google.protobuf.DurationProto.getDescriptor();
            case "google/protobuf/empty.proto" -> com.google.protobuf.EmptyProto.getDescriptor();
            case "google/protobuf/any.proto" -> com.google.protobuf.AnyProto.getDescriptor();
            case "google/protobuf/wrappers.proto" -> com.google.protobuf.WrappersProto.getDescriptor();
            case "google/protobuf/struct.proto" -> com.google.protobuf.StructProto.getDescriptor();
            case "google/protobuf/field_mask.proto" -> com.google.protobuf.FieldMaskProto.getDescriptor();
            case "google/api/annotations.proto" -> com.google.api.AnnotationsProto.getDescriptor();
            case "google/api/http.proto" -> com.google.api.HttpProto.getDescriptor();
            default -> null;
        };
    }

    /**
     * Displays information about REST endpoints defined in the protobuf service annotations
     */
    public void displayRestEndpointInfo(String packageName) {
        try {
            java.util.List<Descriptors.FileDescriptor> fileDescriptors = loadProtobufDescriptor(packageName);

            System.out.println("=== REST Endpoint Information ===");
            System.out.println("File: " + proto);
            System.out.println("Package Filter: " + (packageName != null ? packageName : "all packages"));
            System.out.println("Found " + fileDescriptors.size() + " file descriptor(s)");
            System.out.println();

            // Iterate through all file descriptors
            for (Descriptors.FileDescriptor fileDescriptor : fileDescriptors) {
                System.out.println("Package: " + fileDescriptor.getPackage());
                System.out.println("File Name: " + fileDescriptor.getName());
                System.out.println();

                // Iterate through all services in each file descriptor
                for (Descriptors.ServiceDescriptor service : fileDescriptor.getServices()) {
                    System.out.println("Service: " + service.getName());
                    System.out.println("Full Name: " + service.getFullName());
                    System.out.println();

                    // Iterate through all methods in the service
                    for (Descriptors.MethodDescriptor method : service.getMethods()) {
                        System.out.println("  Method: " + method.getName());
                        System.out.println("  Input Type: " + method.getInputType().getName());
                        System.out.println("  Output Type: " + method.getOutputType().getName());

                        // Check for HTTP annotations
                        if (method.getOptions().hasExtension(AnnotationsProto.http)) {
                            com.google.api.HttpRule httpRule = method.getOptions().getExtension(AnnotationsProto.http);

                            System.out.println("  HTTP Method: " + getHttpMethod(httpRule));
                            System.out.println("  HTTP Path: " + getHttpPath(httpRule));

                            if (!httpRule.getBody().isEmpty()) {
                                System.out.println("  HTTP Body: " + httpRule.getBody());
                            }

                            // Display additional bindings if any
                            for (com.google.api.HttpRule additionalBinding : httpRule.getAdditionalBindingsList()) {
                                System.out.println("  Additional Binding:");
                                System.out.println("    HTTP Method: " + getHttpMethod(additionalBinding));
                                System.out.println("    HTTP Path: " + getHttpPath(additionalBinding));
                            }
                        } else {
                            System.out.println("  No HTTP annotations found");
                        }

                        System.out.println();
                    }

                    System.out.println("---");
                }

                System.out.println();
            }

        } catch (IOException e) {
            System.err.println("Error reading protobuf descriptor file: " + e.getMessage());
        } catch (Descriptors.DescriptorValidationException e) {
            System.err.println("Error validating protobuf descriptor: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Extracts the HTTP method from an HttpRule
     */
    private String getHttpMethod(com.google.api.HttpRule httpRule) {
        return switch (httpRule.getPatternCase()) {
            case GET -> "GET";
            case PUT -> "PUT";
            case POST -> "POST";
            case DELETE -> "DELETE";
            case PATCH -> "PATCH";
            case CUSTOM -> httpRule.getCustom().getKind();
            default -> "UNKNOWN";
        };
    }

    /**
     * Extracts the HTTP path from an HttpRule
     */
    protected String getHttpPath(com.google.api.HttpRule httpRule) {
        return switch (httpRule.getPatternCase()) {
            case GET -> httpRule.getGet();
            case PUT -> httpRule.getPut();
            case POST -> httpRule.getPost();
            case DELETE -> httpRule.getDelete();
            case PATCH -> httpRule.getPatch();
            case CUSTOM -> httpRule.getCustom().getPath();
            default -> "";
        };
    }
}
