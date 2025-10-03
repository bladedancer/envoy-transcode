package com.matthews.poc.transcode;

import com.google.protobuf.Any;
import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.Person;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        description = "Removes an object from the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class Remove extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id to remove", required = true)
    Integer id;

    @Override
    @SneakyThrows
    public void run() {
        Any removed = getAnyService().removeAny(ById.newBuilder().setId(id).build());

        if (removed == null) {
            log.error("No object found with id: {}", id);
        } else if (removed.is(Person.class)) {
            Person person = removed.unpack(Person.class);
            log.info("Removed person: {}", person.getName());
        } else if (removed.is(Address.class)) {
            Address address = removed.unpack(Address.class);
            log.info("Removed address: {}", address.getName());
        } else {
            log.info("Removed object of type: {}", removed.getTypeUrl());
        }
    }
}