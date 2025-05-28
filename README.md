# Descriptor

Descriptor build by maven isn't good enough.

    protoc -I./proto/target/protoc-dependencies/efdb8561f44358d9ebe2a9723a7044b0 -I. --include_imports --include_source_info     --descriptor_set_out=config/proto.pb proto/src/main/proto/contact.proto