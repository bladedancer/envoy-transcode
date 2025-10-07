# Descriptor

Descriptor build by maven isn't good enough.

    protoc -I./proto/target/protoc-dependencies/efdb8561f44358d9ebe2a9723a7044b0 -I. -I./proto/src/main/proto --include_imports --include_source_info --descriptor_set_out=config/proto.pb \
    proto/src/main/proto/anything.proto proto/src/main/proto/address.proto proto/src/main/proto/contact.proto 

Run the server:
    
    java -jar server/target/server-1.0.0.jar

Run Envoy (func-e hasn't got latest so not always applicable):

    func-e run --config-file=config/envoy.yaml

Run Envoy in docker:

    docker run --rm --net host --name envoy-transcode -v "$(pwd)/config:/config" \
      envoyproxy/envoy:v1.34.4 envoy -c /config/envoy.yaml

Call from client (no envoy):

    java -jar client/target/client-1.0.0.jar add-person -h localhost -p 9090 -n gavin -e gavin@example.com -t 1234

    java -jar client/target/client-1.0.0.jar get-person -h localhost -p 9090 -i <id>

    java -jar client/target/client-1.0.0.jar add-address -h localhost -p 9090 -n myaddress --number 3 -s mystreet -c mycity -z 1

Test with curl:

    curl -ik http://localhost:19090/any/78

    curl -ik -X POST http://localhost:19090/any -H "Content-Type: application/json" -d '{"@type": "type.googleapis.com/demo.Person","name": "new","email": "new@example.com","phones": [{ "number": "1234", "type": "HOME" }]}'

Envoy debug log

    curl -X POST http://localhost:9901/logging?level=error && curl -X POST http://localhost:9901/logging?paths=http2:debug,connection:debug,router:debug