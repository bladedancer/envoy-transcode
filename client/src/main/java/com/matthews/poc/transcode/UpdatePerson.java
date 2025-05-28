package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.Person;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "update",
        description = "Updates a person to the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class UpdatePerson extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id of the person", required = true)
    Integer id;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Name of the person", required = true)
    String name;

    @CommandLine.Option(names = {"-e", "--email"}, description = "Email address of the person", required = true)
    String email;

    @CommandLine.Option(names = {"-t", "--telephone"}, description = "Telephone number of the person", required = true)
    String number;

    @Override
    public void run() {
        getContactService().updatePerson(Person.newBuilder()
                .setId(id)
                .setName(name)
                .setEmail(email)
                .addPhones(Person.PhoneNumber.newBuilder()
                        .setNumber(number)
                        .setType(Person.PhoneType.HOME)
                        .build())
                .build());
        log.info("Updated person with id: {}", id);
    }
}