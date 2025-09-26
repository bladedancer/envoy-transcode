package com.matthews.poc.basepath;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
    InfoCommand.class,
    RewriteCommand.class
})
public class Main {
}
