package org.soitoolkit.commons.mule.core;

/**
 * Common names.
 * 
 * @author Magnus Larsson
 *
 */
public interface PropertyNames {

	// Note that the values of these constans must be compliant with the JMS specification, see section 3.5.1 and 3.8.1.1.
	public static final String SOITOOLKIT_INTEGRATION_SCENARIO = "soitoolkit_integrationScenario"; 
	public static final String SOITOOLKIT_CORRELATION_ID = "soitoolkit_correlationId"; 
	public static final String SOITOOLKIT_CONTRACT_ID = "soitoolkit_contractId"; 
	
	public final static String DEFAULT_MULE_JMS_CONNECTOR = "soitoolkit-jms-connector";
	public final static String DEFAULT_MULE_JDBC_DATASOURCE = "soitoolkit-jdbc-datasource";

	
}
