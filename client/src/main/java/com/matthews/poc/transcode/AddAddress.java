package com.matthews.poc.transcode;

import com.google.protobuf.Any;
import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.ById;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "add-address",
        description = "Adds an address to the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class AddAddress extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Name of the address", required = true)
    String name;

    @CommandLine.Option(names = {"--number"}, description = "Street number", required = false, defaultValue = "")
    String number;

    @CommandLine.Option(names = {"-s", "--street"}, description = "Street name", required = true)
    String street;

    @CommandLine.Option(names = {"-c", "--city"}, description = "City name", required = true)
    String city;

    @CommandLine.Option(names = {"-z", "--zip"}, description = "Postal zip code", required = true)
    String zip;

    @CommandLine.Option(names = {"--country"}, description = "Country", required = false, defaultValue = "IE")
    String country;

    @Override
    public void run() {
        ById id = getAnyService().addAny(Any.pack(Address.newBuilder()
                .setName(name)
                .setHome(Address.Postal.newBuilder()
                        .setNumber(number)
                        .setStreet(street)
                        .setCity(city)
                        .setZip(zip)
                        .setCountry(country)
                        .build())
                .build()));
        log.info("Added address with id: {}", id.getId());
    }
}