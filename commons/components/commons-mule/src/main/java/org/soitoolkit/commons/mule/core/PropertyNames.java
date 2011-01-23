/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public static final String SOITOOLKIT_BUSINESS_CONTEXT_ID = "soitoolkit_businessContextId"; 

	public final static String DEFAULT_MULE_JMS_CONNECTOR = "soitoolkit-jms-connector";
	public final static String DEFAULT_MULE_JDBC_DATASOURCE = "soitoolkit-jdbc-datasource";
}