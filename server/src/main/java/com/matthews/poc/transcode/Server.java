package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.ContactServiceGrpc;
import io.grpc.ServerBuilder;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.validator.constraints.Range;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Startup
@ApplicationScoped
@Slf4j
public class Server {
    @Inject
    @Range(min = 1024, max = 65535)
    @ConfigProperty(name = "grpc.port", defaultValue = "9090")
    Integer port;

    private io.grpc.Server server;

    @Inject
    public void init(final ContactServiceGrpc.ContactServiceImplBase contactService) {
        log.info("Initializing service");
        server = ServerBuilder.forPort(port)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .addService(contactService)
                .build();
    }

    @PostConstruct
    public void start() throws IOException {
        server.start();
        log.info("Server started, listening on " + port);
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            server = null;
        }
    }
}
