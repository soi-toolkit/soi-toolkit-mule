mkdir private
mkdir public
mkdir client

# CA
--openssl genrsa -des3 -passout pass:password -out private/ca.key 1024
openssl genrsa -des3 -passout pass:password -out private/ca.key 1024
 
--openssl req -new -x509 -key private/ca.key -out public/ca.crt -days 3600
openssl req -new -x509 -key private/ca.key -passin pass:password -out public/ca.crt -days 3600


# 2. Create and sign your server certificate
# 2.1 Creation of the server certificate 
openssl genrsa -des3 -passout pass:password -out private/server.key 1024
openssl req -new -key private/server.key -passin pass:password -out server.csr
openssl x509 -req -days 3600 -in server.csr -CA public/ca.crt -CAkey private/ca.key 
-CAcreateserial -out public/server.crt
openssl pkcs12 -export -in public/server.crt -inkey private/server.key -out server.p12

keytool -v -importkeystore -srckeystore server.p12 -srcstoretype PKCS12 -destkeystore server.jks -deststoretype JKS

# 3. Client authentication
# 3.1 Client certificate creation and signing
openssl req -new -newkey rsa:1024 -nodes -passout pass:password -out client/client.req -keyout client/client.key
openssl x509 -CA public/ca.crt -CAkey private/ca.key -CAserial public/ca.srl -req -in client/client.req -passin pass:password -out client/client.pem -days 3600

# 3.2 Export client certificate as keychain in pkcs12/java keystore
openssl pkcs12 -export -clcerts -in client/client.pem -inkey client/client.key -out client/client.p12 -name consumer

keytool -v -importkeystore -srckeystore client/client.p12 -srcstoretype PKCS12 -destkeystore client.jks -deststoretype JKS

keytool -genkey -alias dummy -keyalg RSA -keystore truststore.jks

keytool -import -v -trustcacerts -alias my_ca -file public/ca.crt -keystore truststore.jks

keytool -v -list -keystore truststore.jks

