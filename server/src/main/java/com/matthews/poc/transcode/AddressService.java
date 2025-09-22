package com.matthews.poc.transcode;

import com.google.protobuf.Empty;
import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.AddressServiceGrpc;
import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.Count;
import io.grpc.stub.StreamObserver;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Startup
@Slf4j
public class AddressService extends AddressServiceGrpc.AddressServiceImplBase {

    private final Random rand = new Random(System.currentTimeMillis());
    private final ObservableMap<Integer, Address> addresses = new ObservableMap<>(new HashMap<>());

    /**
     */
    @Override
    public void getAddress(com.matthews.poc.transcode.protos.ById request,
                          io.grpc.stub.StreamObserver<com.matthews.poc.transcode.protos.Address> responseObserver) {
        log.info("getAddress called with id: " + request.getId());
        if (addresses.containsKey(request.getId())) {
            Address address = addresses.get(request.getId());
            responseObserver.onNext(address);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new IllegalArgumentException("Address not found with id: " + request.getId()));
        }
    }

    @Override
    public void addAddress(Address request, StreamObserver<ById> responseObserver) {
        log.info("addAddress called with name: " + request.getName());
        int id = rand.nextInt(1000);
        request = request.toBuilder().setId(id).build();
        addresses.put(id, request);
        ById response = ById.newBuilder().setId(id).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void removeAddress(ById request, StreamObserver<Address> responseObserver) {
        log.info("removeAddress called with id: " + request.getId());
        Address address = addresses.remove(request.getId());
        responseObserver.onNext(address);
        responseObserver.onCompleted();
    }


    @Override
    public void updateAddress(Address request, StreamObserver<Empty> responseObserver) {
        log.info("updateAddress called with id: " + request.getId() + " and name: " + request.getName());
        if (addresses.containsKey(request.getId())) {
            addresses.put(request.getId(), request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new IllegalArgumentException("Address not found with id: " + request.getId()));
        }
    }

    @Override
    public void watchAddress(Count request, StreamObserver<Address> responseObserver) {
        // Hack - send 3 updates
        log.info("watchPeople called");
        AtomicInteger count = new AtomicInteger(request.getCount());
        Cancellable[] cancellable = new Cancellable[1];
        cancellable[0] = addresses.observe().subscribe().with(event -> {
            responseObserver.onNext(event.value());
            if (count.decrementAndGet() <= 0) {
                responseObserver.onCompleted();
                cancellable[0].cancel();
            }
        }, error -> {
            log.error("Error observing people", error);
            responseObserver.onError(error);
        });
    }
}
