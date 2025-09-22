package com.matthews.poc.transcode;

import com.matthews.poc.transcode.protos.Address;
import com.matthews.poc.transcode.protos.ById;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
        name = "remove-address",
        description = "Removes an address from the system",
        mixinStandardHelpOptions = true,
        version = "1.0")
@Slf4j
public class RemoveAddress extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Id of the address", required = true)
    Integer id;

    @Override
    public void run() {
        Address removed = getAddressService().removeAddress(ById.newBuilder().setId(id).build());
        if (removed == null) {
            log.error("No address found with id: {}", id);
        } else {
            log.info("Removed {} with id {}", removed.getName(), removed.getId());
        }
    }
}