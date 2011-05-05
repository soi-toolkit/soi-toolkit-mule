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
package org.soitoolkit.commons.mule.api.log;

import java.util.Map;

import org.mule.api.MuleMessage;

// TODO: document which attributes are mandatory/optional
public class EventLogMessage {
	private MuleMessage muleMessage;
	private String logMessage;
	private String integrationScenario;
	private String contractId;
	private Map<String, String> businessContextId;
	private Map<String, String> extraInfo;

	public MuleMessage getMuleMessage() {
		return muleMessage;
	}

	public void setMuleMessage(MuleMessage muleMessage) {
		this.muleMessage = muleMessage;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	public String getIntegrationScenario() {
		return integrationScenario;
	}

	public void setIntegrationScenario(String integrationScenario) {
		this.integrationScenario = integrationScenario;
	}

	public String getContractId() {
		return contractId;
	}

	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	public Map<String, String> getBusinessContextId() {
		return businessContextId;
	}

	public void setBusinessContextId(Map<String, String> businessContextId) {
		this.businessContextId = businessContextId;
	}

	public Map<String, String> getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(Map<String, String> extraInfo) {
		this.extraInfo = extraInfo;
	}

}
