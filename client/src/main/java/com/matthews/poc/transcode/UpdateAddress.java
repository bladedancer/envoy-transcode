package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.Address;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "update-address",
        description = "Updates an address in the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class UpdateAddress extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id of the person", required = true)
    Integer id;

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
        getAddressService().updateAddress(Address.newBuilder()
                .setName(name)
                .setHome(Address.Postal.newBuilder()
                        .setNumber(number)
                        .setStreet(street)
                        .setCity(city)
                        .setZip(zip)
                        .setCountry(country)
                        .build())
                .build());
        log.info("Updated address with id: {}", id);
    }
}