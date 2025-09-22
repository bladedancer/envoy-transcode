package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.AddressServiceGrpc;
import com.matthews.poc.transcode.protos.ContactServiceGrpc;
import picocli.CommandLine;

public class BaseCommand {
    @CommandLine.Option(names = {"-h", "--host"}, description = "Server hostname", defaultValue = "localhost")
    String hostname;

    @CommandLine.Option(names = {"-p", "--port"}, description = "Server port", defaultValue = "9090")
    Integer port;

    protected ContactServiceGrpc.ContactServiceBlockingStub getContactService() {
        return ContactServiceGrpc.newBlockingStub(
                io.grpc.ManagedChannelBuilder.forAddress(hostname, port)
                        .usePlaintext()
                        .build());
    }

    protected AddressServiceGrpc.AddressServiceBlockingStub getAddressService() {
        return AddressServiceGrpc.newBlockingStub(
                io.grpc.ManagedChannelBuilder.forAddress(hostname, port)
                        .usePlaintext()
                        .build());
    }
}
