package com.matthews.poc.transcode;

import com.google.protobuf.Empty;
import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.ContactServiceGrpc;
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
public class ContactService extends ContactServiceGrpc.ContactServiceImplBase {

    private final Random rand = new Random(System.currentTimeMillis());
    private final ObservableMap<Integer, Person> people = new ObservableMap<>(new HashMap<>());

    /**
     */
    @Override
    public void getPerson(com.matthews.poc.transcode.protos.ById request,
                           io.grpc.stub.StreamObserver<com.matthews.poc.transcode.protos.Person> responseObserver) {
        log.info("getPerson called with id: " + request.getId());
        if (people.containsKey(request.getId())) {
            Person person = people.get(request.getId());
            responseObserver.onNext(person);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new IllegalArgumentException("Person not found with id: " + request.getId()));
        }
    }

    @Override
    public void addPerson(Person request, StreamObserver<ById> responseObserver) {
        log.info("addPerson called with name: " + request.getName());
        int id = rand.nextInt(1000);
        request = request.toBuilder().setId(id).build();
        people.put(id, request);
        ById response = ById.newBuilder().setId(id).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void removePerson(ById request, StreamObserver<Person> responseObserver) {
        log.info("removePerson called with id: " + request.getId());
        Person person = people.remove(request.getId());
        responseObserver.onNext(person);
        responseObserver.onCompleted();
    }


    @Override
    public void updatePerson(Person request, StreamObserver<Empty> responseObserver) {
        log.info("updatePerson called with id: " + request.getId() + " and name: " + request.getName());
        if (people.containsKey(request.getId())) {
            people.put(request.getId(), request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new IllegalArgumentException("Person not found with id: " + request.getId()));
        }
    }

    @Override
    public void watchPeople(Count request, StreamObserver<Person> responseObserver) {
        // Hack - send 3 updates
        log.info("watchPeople called");
        AtomicInteger count = new AtomicInteger(request.getCount());
        Cancellable[] cancellable = new Cancellable[1];
        cancellable[0] = people.observe().subscribe().with(event -> {
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
