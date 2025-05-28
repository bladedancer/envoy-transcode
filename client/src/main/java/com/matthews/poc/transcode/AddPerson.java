package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.Person;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
    name = "add",
    description = "Adds a person to the system",
    mixinStandardHelpOptions = true,
    version = "1.0")
@Slf4j
public class AddPerson extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Name of the person", required = true)
    String name;

    @CommandLine.Option(names = {"-e", "--email"}, description = "Email address of the person", required = true)
    String email;

    @CommandLine.Option(names = {"-t", "--telephone"}, description = "Telephone number of the person", required = true)
    String number;

    @Override
    public void run() {
        ById id = getContactService().addPerson(Person.newBuilder()
                        .setName(name)
                        .setEmail(email)
                        .addPhones(Person.PhoneNumber.newBuilder()
                                .setNumber(number)
                                .setType(Person.PhoneType.HOME)
                                .build())
                .build());
        log.info("Added person with id: {}", id.getId());
    }
}