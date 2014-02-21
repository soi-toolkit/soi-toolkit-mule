# create keystore

# validity: 20 years * 365 days --> 7300 days
keytool -genkeypair -alias soitoolkit -dname "CN=SOI Toolkit Encryption Tool, OU=NOT FOR PRODUCTION, O=TEST, C=SE" -keystore soitoolkit-encryption-tool-server.jks -storetype jks -storepass password -keypass password -keyalg RSA -validity 7300

keytool -list -keystore soitoolkit-encryption-tool-server.jks -storepass password -v