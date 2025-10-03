package com.matthews.poc.transcode;

import com.google.protobuf.Any;
import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.Count;
import com.matthews.poc.transcode.protos.Person;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Iterator;

@CommandLine.Command(
        name = "watch",
        description = "Listens for changes",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class Watch extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--count"}, description = "Number of changes to watch for", required = true)
    Integer count;

    @Override
    @SneakyThrows
    public void run() {
        Iterator<Any> anys = getAnyService().watchAny(Count.newBuilder().setCount(count).build());

        while(anys.hasNext()) {
            Any any = anys.next();
            if (any == null) {
                continue;
            } else if (any.is(Person.class)) {
                Person person = any.unpack(Person.class);
                log.info("Name: {}, Email: {}, Phone: {}", person.getName(), person.getEmail(),
                        person.getPhonesList().stream()
                                .map(p -> p.getNumber() + " (" + p.getType() + ")")
                                .reduce((a, b) -> a + ", " + b).orElse("No phone numbers"));
            } else if (any.is(Address.class)) {
                Address address = any.unpack(Address.class);
                log.info("Address: {}, {}, {}, {}, {}, {}", address.getName(),
                        address.getHome().getNumber(), address.getHome().getStreet(),
                        address.getHome().getCity(), address.getHome().getZip(),
                        address.getHome().getCountry());
            } else {
                log.info("Watch object of type: {}", any.getTypeUrl());
            }
        }
    }
}