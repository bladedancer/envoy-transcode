# Descriptor

Descriptor build by maven isn't good enough.

    protoc -I./proto/target/protoc-dependencies/efdb8561f44358d9ebe2a9723a7044b0 -I. --include_imports --include_source_info     --descriptor_set_out=config/proto.pb proto/src/main/proto/contact.proto

Run the server:
    
    java -jar server/target/server-1.0.0.jar

Run Envoy:

    func-e run --config-file=config/envoy.yaml

Call from client (no envoy):

    java -jar client/target/client-1.0.0.jar add-person -h localhost -p 9090 -n gavin -e gavin@example.com -t 1234

    java -jar client/target/client-1.0.0.jar get-person -h localhost -p 9090 -i <id>

    java -jar client/target/client-1.0.0.jar add-address -h localhost -p 9090 -n myaddress --number 3 -s mystreet -c mycity -z 1

Test with curl:

    curl -ik http://localhost:19090/contact/78

    curl -ik http://localhost:19090/address/510

Envoy debug log

    curl 'http://localhost:9901/logging' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data-raw 'paths=&level=debug'