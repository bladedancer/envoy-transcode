package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.Count;
import com.matthews.poc.transcode.protos.Person;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Iterator;

@CommandLine.Command(
        name = "watch",
        description = "Listens for changes",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class WatchPeople extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--count"}, description = "Number of changes to watch for", required = true)
    Integer count;

    @Override
    public void run() {
        Iterator<Person> people = getContactService().watchPeople(Count.newBuilder().setCount(count).build());

        while(people.hasNext()) {
            Person person = people.next();
            log.info("Name: {}, Email: {}, Phone: {}", person.getName(), person.getEmail(),
                    person.getPhonesList().stream()
                            .map(p -> p.getNumber() + " (" + p.getType() + ")")
                            .reduce((a, b) -> a + ", " + b).orElse("No phone numbers"));
        }
    }
}