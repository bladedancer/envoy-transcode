package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.Person;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "get",
        description = "Get a person from the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class GetPerson extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id of the person", required = true)
    Integer id;

    @Override
    public void run() {
        Person person = getContactService().getPerson(ById.newBuilder().setId(id).build());
        if (person == null) {
            log.error("No person found with id: {}", id);
        } else {
            log.info("Name: {}, Email: {}, Phone: {}", person.getName(), person.getEmail(),
                    person.getPhonesList().stream()
                            .map(p -> p.getNumber() + " (" + p.getType() + ")")
                            .reduce((a, b) -> a + ", " + b).orElse("No phone numbers"));
        }
    }
}