package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.Count;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Iterator;

@CommandLine.Command(
        name = "watch-address",
        description = "Listens for changes",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class WatchAddress extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--count"}, description = "Number of changes to watch for", required = true)
    Integer count;

    @Override
    public void run() {
        Iterator<Address> addresses = getAddressService().watchAddress(Count.newBuilder().setCount(count).build());

        while(addresses.hasNext()) {
            Address address = addresses.next();
            log.info("Address: {}, {}, {}, {}, {}, {}", address.getName(),
                    address.getHome().getNumber(), address.getHome().getStreet(),
                    address.getHome().getCity(), address.getHome().getZip(),
                    address.getHome().getCountry());
        }
    }
}