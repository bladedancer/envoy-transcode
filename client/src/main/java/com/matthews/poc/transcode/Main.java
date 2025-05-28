package com.matthews.poc.transcode;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
        GetPerson.class,
        AddPerson.class,
        UpdatePerson.class,
        RemovePerson.class,
        WatchPeople.class
})
public class Main {
}
