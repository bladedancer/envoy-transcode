package com.matthews.poc.transcode;

import com.google.protobuf.Any;
import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.Person;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "get-person",
        description = "Get a person from the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class GetPerson extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id of the person", required = true)
    Integer id;

    @Override
    @SneakyThrows
    public void run() {
        Any any = getAnyService().getAny(ById.newBuilder().setId(id).build());
        if (any.is(Person.class)) {
            Person person = any.unpack(Person.class);
            log.info("Name: {}, Email: {}, Phone: {}", person.getName(), person.getEmail(),
                    person.getPhonesList().stream()
                            .map(p -> p.getNumber() + " (" + p.getType() + ")")
                            .reduce((a, b) -> a + ", " + b).orElse("No phone numbers"));
        } else if (any != null) {
            log.error("Id {} is not a person", id);
        } else {
            log.error("No person found with id: {}", id);
        }
    }
}