package com.matthews.poc.transcode;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
        GetPerson.class,
        AddPerson.class,
        UpdatePerson.class,
        GetAddress.class,
        AddAddress.class,
        UpdateAddress.class,
        Remove.class,
        Watch.class

})
public class Main {
}
