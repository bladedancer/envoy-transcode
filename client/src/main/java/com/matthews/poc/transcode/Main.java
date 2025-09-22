package com.matthews.poc.transcode;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
        GetPerson.class,
        AddPerson.class,
        UpdatePerson.class,
        RemovePerson.class,
        WatchPeople.class,
        GetAddress.class,
        AddAddress.class,
        UpdateAddress.class,
        RemoveAddress.class,
        WatchAddress.class

})
public class Main {
}
