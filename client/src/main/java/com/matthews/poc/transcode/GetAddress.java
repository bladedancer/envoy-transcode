package com.matthews.poc.transcode;

import com.google.protobuf.Any;
import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.ById;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "get-address",
        description = "Get an address from the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class GetAddress extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id of the address", required = true)
    Integer id;

    @Override
    @SneakyThrows
    public void run() {
        Any any = getAnyService().getAny(ById.newBuilder().setId(id).build());
        if (any.is(Address.class)) {
            Address address = any.unpack(Address.class);
            log.info("Address: {}, {}, {}, {}, {}, {}", address.getName(),
                    address.getHome().getNumber(), address.getHome().getStreet(),
                    address.getHome().getCity(), address.getHome().getZip(),
                    address.getHome().getCountry());
        } else if (any != null) {
            log.error("Id {} is not an address", id);
        } else {
            log.error("No address found with id: {}", id);
        }
    }
}