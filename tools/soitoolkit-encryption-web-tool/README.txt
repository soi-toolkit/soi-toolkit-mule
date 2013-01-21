soitoolkit-encryption-web-tool
================================
Description
------------
Mule-webapp to help with property encryption (instead of using command line tools).

See:
http://code.google.com/p/soi-toolkit/wiki/UG_PropertyFile#Encrypted_passwords
http://code.google.com/p/soi-toolkit/issues/detail?id=329


Deploy app to Mule
-------------------
1. Set environment variable SOITOOLKIT_ENCRYPTION_PASSWORD in wrapper conf (ref wiki)

2. Configure SSL/TLS: configure keystore and truststore in an props-override file:
  soitoolkit-encryption-web-tool-config-override.properties
  
  and put the file in dir $MULE_HOME/conf
  
  Example config:
	HTTPS_TLS_KEYSTORE=${mule.home}/conf/soitk-encryption-tool-server.jks
	HTTPS_TLS_KEYSTORE_PASSWORD=password
	HTTPS_TLS_KEY_PASSWORD=password
	HTTPS_TLS_TRUSTSTORE=${mule.home}/conf/soitk-encryption-tool-truststore.jks
	HTTPS_TLS_TRUSTSTORE_PASSWORD=password


3. Deploy *this* Mule-webapp to the Mule-container

4. Access webapp on: https://<HOSTNAME>:9098/soitoolkit-encryption-web-tool/
