package com.matthews.poc.basepath;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "info",
        description = "Displays the http rule info about the Protobuf descriptor",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class InfoCommand extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--package"}, description = "Protobuf package", required = true)
    String packageName;

    @Override
    public void run() {
        displayRestEndpointInfo(packageName);
    }
}
