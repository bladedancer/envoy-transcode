package com.matthews.poc.transcode;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.AnyServiceGrpc;
import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.Count;
import com.matthews.poc.transcode.protos.Person;
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
public class AnyService extends AnyServiceGrpc.AnyServiceImplBase {

    private final Random rand = new Random(System.currentTimeMillis());
    private final ObservableMap<Integer, Message> anys = new ObservableMap<>(new HashMap<>());

    /**
     */
    @Override
    public void getAny(com.matthews.poc.transcode.protos.ById request,
                          io.grpc.stub.StreamObserver<Any> responseObserver) {
        log.info("getAny called with id: " + request.getId());
        if (anys.containsKey(request.getId())) {
            Any thing = Any.pack(anys.get(request.getId()));
            responseObserver.onNext(thing);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new IllegalArgumentException("Any not found with id: " + request.getId()));
        }
    }

    @Override
    public void addAny(Any request, StreamObserver<ById> responseObserver) {
        log.info("addAny called");
        int id = rand.nextInt(1000);

        try {
            Message message;
            if (request.is(Person.class)) {
                Person person = request.unpack(Person.class);
                // Create a new Person with the generated ID
                message = person.toBuilder().setId(id).build();
                log.info("Unpacked and stored Person with id: " + id);
            } else if (request.is(Address.class)) {
                Address address = request.unpack(Address.class);
                // Create a new Address with the generated ID
                message = address.toBuilder().setId(id).build();
                log.info("Unpacked and stored Address with id: " + id);
            } else {
                responseObserver.onError(new IllegalArgumentException("Unsupported message type"));
                return;
            }

            anys.put(id, message);
            ById response = ById.newBuilder().setId(id).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error unpacking Any message", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void removeAny(ById request, StreamObserver<Any> responseObserver) {
        log.info("removeAny called with id: {}", request.getId());
        Any thing = Any.pack(anys.remove(request.getId()));
        responseObserver.onNext(thing);
        responseObserver.onCompleted();
    }

    @Override
    public void updateAny(Any request, StreamObserver<Empty> responseObserver) {
        log.info("updateAny called");
        try {
            if (request.is(Person.class)) {
                Person person = request.unpack(Person.class);
                if (!anys.containsKey(person.getId())) {
                    responseObserver.onError(new IllegalArgumentException("Person not found with id: " + person.getId()));
                    return;
                }
                anys.put(person.getId(), person);
                log.info("Updated Person with id: {}", person.getId());
            } else if (request.is(Address.class)) {
                Address address = request.unpack(Address.class);
                if (!anys.containsKey(address.getId())) {
                    responseObserver.onError(new IllegalArgumentException("Address not found with id: " + address.getId()));
                    return;
                }
                anys.put(address.getId(), address);
                log.info("Updated stored Address with id: {}", address.getId());
            } else {
                responseObserver.onError(new IllegalArgumentException("Unsupported message type"));
                return;
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error unpacking Any message", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void watchAny(Count request, StreamObserver<Any> responseObserver) {
        // Hack - send 3 updates
        log.info("watchAny called");
        AtomicInteger count = new AtomicInteger(request.getCount());
        Cancellable[] cancellable = new Cancellable[1];
        cancellable[0] = anys.observe().subscribe().with(event -> {
            responseObserver.onNext(Any.pack(event.value()));
            if (count.decrementAndGet() <= 0) {
                responseObserver.onCompleted();
                cancellable[0].cancel();
            }
        }, error -> {
            log.error("Error observing anys", error);
            responseObserver.onError(error);
        });
    }
}
