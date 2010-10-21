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
package org.soitoolkit.commons.mule.monitor;

import java.util.Date;
import java.util.ResourceBundle;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.monitor.schema.v1.MonitorInfoType;
import org.soitoolkit.commons.monitor.schema.v1.MonitorInfoType.ExtraInfo;
import org.soitoolkit.commons.monitor.schema.v1.ObjectFactory;
import org.soitoolkit.commons.mule.util.XmlUtil;

/**
 * Base class for a REST based monitor services, based on JAX-RS.
 * 
 * @author Magnus Larsson
 *
 */
@Path("/monitor")
public class MonitorService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final static ObjectFactory OF = new ObjectFactory();

    private String artifactId = null;
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

    /**
     * Example: 
     *   HTTP GET on http://localhost:12000/infobus/kund3/rest/v1/monitor/xml
     *   
     * @return for example
     *   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     *   	<requestMetadata xmlns="urn:se.volvofinans.gim.it:v1">
     *   		<serviceInvocationMetadata>
     *   			<businessContextId>
     *   				<name>Timestamp</name>
     *   				<value>Wed Mar 17 10:55:49 CET 2010</value>
     *   			</businessContextId>
     *   		</serviceInvocationMetadata>
     *   	</requestMetadata>
     *   
     */
	@GET
//	@Produces({"application/xml", "application/json"}) is not yet supported so we publish two implementations, one for each format...
	@Produces("application/xml")
    @Path("xml")
    public JAXBElement<MonitorInfoType> pingXml() {
		return ping();
    }

    /**
     * Example: 
     *   HTTP GET on http://localhost:12000/infobus/kund3/rest/v1/monitor/json
     *   
     * @return for example
     *   {"serviceInvocationMetadata":{"businessContextId":{"name":"Timestamp","value":"Wed Mar 17 10:58:21 CET 2010"}}}
     */
	@GET
//	@Produces({"application/xml", "application/json"}) is not yet supported so we publish two implementations, one for each format...
	@Produces("application/json")
    @Path("json")
    public JAXBElement<MonitorInfoType> pingJson() {
		return ping();
    }



    @GET
    @Produces("application/xml")
    @Path("services")
    public JAXBElement<MonitorInfoType> pingServicesDefault(
            @DefaultValue("false") @QueryParam("errorsOnly") String errorsOnly) {
        return pingServices(toBoolean(errorsOnly));
    }

    @GET
    @Produces("application/xml")
    @Path("services/xml")
    public JAXBElement<MonitorInfoType> pingServicesXml(
            @DefaultValue("false") @QueryParam("errorsOnly") String errorsOnly) {
        return pingServices(toBoolean(errorsOnly));
    }

    @GET
    @Produces("application/json")
    @Path("services/json")
    public JAXBElement<MonitorInfoType> pingServicesJson(
            @DefaultValue("false") @QueryParam("errorsOnly") String errorsOnly) {
        return pingServices(toBoolean(errorsOnly));
    }

	protected JAXBElement<MonitorInfoType> ping() {
		MonitorInfoType ping = new MonitorInfoType();
		ping.setName(artifactId);
		ping.setVersion(getComponentVersion(artifactId));
		ping.setTimestamp(XmlUtil.convertDateToXmlDate(new Date()));
		if (logger.isDebugEnabled()) {
			logger.debug("Ping returned for {} v{} at {}", new Object[] {ping.getName(), ping.getVersion(), ping.getTimestamp()});
		}
		return OF.createMonitorInfo(ping);
    }

    
    /**
     * Default implementation, expected to be overriden by IC specific monitor services that return info regarding its endpoints
     * 
     * @param errorsOnly
     * @return
     */
    protected JAXBElement<MonitorInfoType> pingServices(boolean errorsOnly) {

        JAXBElement<MonitorInfoType> result = ping();

        return result;
    }

	
	private boolean toBoolean(String errorsOnly) {
		return Boolean.parseBoolean(errorsOnly);
	}
	
	private String getComponentVersion(String artifactId) {
		String bundleName = artifactId + "-build";
		String unknownVersion = "[unknown version]";
		try {
			ResourceBundle rb = ResourceBundle.getBundle(bundleName);
			String version = rb.getString("soitoolkit.buildinfo.version");
			return version;
		} catch (Exception e) {
			logger.warn("Could not find resource bundle {}, return {}", bundleName + ".properties", unknownVersion);
			return unknownVersion;
		}
	}


	protected void addJmsEndpointInfo(JAXBElement<MonitorInfoType> result, boolean showErrorsOnly, String queueName) {
		addExtraInfo(result, showErrorsOnly, "jms", queueName, MonitorEndpointHelper.pingJmsEndpoint(queueName));
	}

	protected void addJmsBackoutQueueInfo(JAXBElement<MonitorInfoType> result, boolean showErrorsOnly, String queueName) {
		addExtraInfo(result, showErrorsOnly, "jms", queueName, MonitorEndpointHelper.pingJmsBackoutQueue(queueName));
	}
	
	protected void addJdbcEndpointInfo(JAXBElement<MonitorInfoType> result, boolean showErrorsOnly, String tableName) {
		addExtraInfo(result, showErrorsOnly, "jdbc", tableName, MonitorEndpointHelper.pingJdbcEndpoint(tableName));
	}

	protected void addSoapHttpEndpointInfo(JAXBElement<MonitorInfoType> result, boolean showErrorsOnly, String soapHttpUrl) {
		addExtraInfo(result, showErrorsOnly, "soap-http", soapHttpUrl, MonitorEndpointHelper.pingSoapHttpUrl(soapHttpUrl));
	}

	private void addExtraInfo(JAXBElement<MonitorInfoType> result, boolean showErrorsOnly, String protocol, String endpointName, String endpointStatus) {

		// Skip add extra info if showErrorsOnly is set to true and the endpoints status is OK.
		if (showErrorsOnly && endpointStatus.startsWith(MonitorEndpointHelper.OK_PREFIX)) return;

		ExtraInfo ei;
		ei = new ExtraInfo();
		if (protocol != null) endpointName = protocol + ":" + endpointName;
		ei.setName(endpointName);
		ei.setValue(endpointStatus);
		result.getValue().getExtraInfo().add(ei);
	}		


}