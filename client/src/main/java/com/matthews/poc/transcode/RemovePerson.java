package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.ById;
import com.matthews.poc.transcode.protos.Person;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        description = "Removes a person from the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class RemovePerson extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id of the person", required = true)
    Integer id;

    @Override
    public void run() {
        Person removed = getContactService().removePerson(ById.newBuilder().setId(id).build());
        if (removed == null) {
            log.error("No person found with id: {}", id);
        } else {
            log.info("Removed {} with id {}", removed.getName(), removed.getId());
        }
    }
}