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
package org.soitoolkit.tools.generator.plugin.model;

public class XmlNamespaceModel {

	@SuppressWarnings("unused")
	private IModel model;
	
	public XmlNamespaceModel(IModel model) {
		this.model = model;
	}
	
	/**
	 * Generates xsd namespaces for files of type: ${artifactId}-common.xml
	 * @return
	 */
	public String getCommon() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txmlns:jms=\"http://www.mulesource.org/schema/mule/jms/2.2\"\n");
		if (model.isJdbc()) {
			sb.append("\txmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\"\n");
		}
		if (model.isServlet()) {
			sb.append("\txmlns:servlet=\"http://www.mulesource.org/schema/mule/servlet/2.2\"\n");
		}
		if (model.isImap()) {
			sb.append("\txmlns:imap=\"http://www.mulesource.org/schema/mule/imap/2.2\"\n");
		}
		if (model.isPop3()) {
			sb.append("\txmlns:pop3=\"http://www.mulesource.org/schema/mule/pop3/2.2\"\n");
		}
		sb.append("\txmlns:management=\"http://www.mulesource.org/schema/mule/management/2.2\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans          http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2       http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jms/2.2        http://www.mulesource.org/schema/mule/jms/2.2/mule-jms.xsd\n");
		if (model.isJdbc()) {
			sb.append("\t\thttp://www.mulesource.org/schema/mule/jdbc/2.2       http://www.mulesource.org/schema/mule/jdbc/2.2/mule-jdbc.xsd\n");
		}
		if (model.isServlet()) {
			sb.append("\t\thttp://www.mulesource.org/schema/mule/servlet/2.2    http://www.mulesource.org/schema/mule/servlet/2.2/mule-servlet.xsd\n");
		}
		if (model.isPop3()) {
			sb.append("\t\thttp://www.mulesource.org/schema/mule/pop3/2.2       http://www.mulesource.org/schema/mule/pop3/2.2/mule-pop3.xsd\n");
		}
		if (model.isImap()) {
			sb.append("\t\thttp://www.mulesource.org/schema/mule/imap/2.2       http://www.mulesource.org/schema/mule/imap/2.2/mule-imap.xsd\n");
		}
		sb.append("\t\thttp://www.mulesource.org/schema/mule/management/2.2 http://www.mulesource.org/schema/mule/management/2.2/mule-management.xsd\n");
		sb.append("\t\">");
		
		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${artifactId}-config.xml
	 * @return
	 */
	public String getServicesOnlyConfig() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2 http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\">");
		
		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${artifactId}-jdbc-connector.xml
	 * @return
	 */
	public String getJdbcConnector() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2 http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jdbc/2.2 http://www.mulesource.org/schema/mule/jdbc/2.2/mule-jdbc.xsd\n");
		sb.append("\t\">");
		
		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${artifactId}-teststubs-and-services-config.xml
	 * @return
	 */
	public String getTeststubsAndServicesConfig() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2 http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\">");
		
		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${artifactId}-teststubs-only-config.xml
	 * @return
	 */
	public String getTeststubsOnlyConfig() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2 http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\">");
		
		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${service}-service.xml
	 * @return
	 */
	public String getOnewayService() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txmlns:vm=\"http://www.mulesource.org/schema/mule/vm/2.2\"\n");
		sb.append("\txmlns:file=\"http://www.mulesource.org/schema/mule/file/2.2\"\n");
		sb.append("\txmlns:ftp=\"http://www.mulesource.org/schema/mule/ftp/2.2\"\n");
		sb.append("\txmlns:sftp=\"http://www.mulesource.org/schema/mule/sftp/2.2\"\n");
		sb.append("\txmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\"\n");
		sb.append("\txmlns:email=\"http://www.mulesource.org/schema/mule/email/2.2\"\n");
		sb.append("\txmlns:imap=\"http://www.mulesource.org/schema/mule/imap/2.2\"\n");
		sb.append("\txmlns:pop3=\"http://www.mulesource.org/schema/mule/pop3/2.2\"\n");
		sb.append("\txmlns:smtp=\"http://www.mulesource.org/schema/mule/smtp/2.2\"\n");
		sb.append("\txmlns:jms=\"http://www.mulesource.org/schema/mule/jms/2.2\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans     http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2  http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/vm/2.2    http://www.mulesource.org/schema/mule/vm/2.2/mule-vm.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/file/2.2  http://www.mulesource.org/schema/mule/file/2.2/mule-file.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/ftp/2.2   http://www.mulesource.org/schema/mule/ftp/2.2/mule-ftp.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/sftp/2.2  http://www.mulesource.org/schema/mule/sftp/2.2/mule-sftp.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jdbc/2.2  http://www.mulesource.org/schema/mule/jdbc/2.2/mule-jdbc.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/email/2.2 http://www.mulesource.org/schema/mule/email/2.2/mule-email.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/imap/2.2  http://www.mulesource.org/schema/mule/imap/2.2/mule-imap.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/pop3/2.2  http://www.mulesource.org/schema/mule/pop3/2.2/mule-pop3.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/smtp/2.2  http://www.mulesource.org/schema/mule/smtp/2.2/mule-smtp.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jms/2.2   http://www.mulesource.org/schema/mule/jms/2.2/mule-jms.xsd\n");
		sb.append("\t\">");

		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${service}-teststub-service.xml
	 * @return
	 */
	public String getOnewayTeststubService() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txmlns:file=\"http://www.mulesource.org/schema/mule/file/2.2\"\n");
		sb.append("\txmlns:ftp=\"http://www.mulesource.org/schema/mule/ftp/2.2\"\n");
		sb.append("\txmlns:sftp=\"http://www.mulesource.org/schema/mule/sftp/2.2\"\n");
		sb.append("\txmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\"\n");
		sb.append("\txmlns:imap=\"http://www.mulesource.org/schema/mule/imap/2.2\"\n");
		sb.append("\txmlns:jms=\"http://www.mulesource.org/schema/mule/jms/2.2\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans     http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2  http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/vm/2.2    http://www.mulesource.org/schema/mule/vm/2.2/mule-vm.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/ftp/2.2   http://www.mulesource.org/schema/mule/ftp/2.2/mule-ftp.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/sftp/2.2  http://www.mulesource.org/schema/mule/sftp/2.2/mule-sftp.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jdbc/2.2  http://www.mulesource.org/schema/mule/jdbc/2.2/mule-jdbc.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/imap/2.2  http://www.mulesource.org/schema/mule/imap/2.2/mule-imap.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jms/2.2   http://www.mulesource.org/schema/mule/jms/2.2/mule-jms.xsd\n");
		sb.append("\t\">");

		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${service}--service.xml
	 * @return
	 */
	public String getReqRespService() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:jdbc=\"http://www.mulesource.org/schema/mule/vm/2.2\"\n");
		sb.append("\txmlns:cxf=\"http://www.mulesource.org/schema/mule/cxf/2.2\"\n");
		sb.append("\txmlns:jms=\"http://www.mulesource.org/schema/mule/jms/2.2\"\n");
		sb.append("\txmlns:smooks=\"http://www.muleforge.org/smooks/schema/mule-module-smooks/1.2\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans     http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2  http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/cxf/2.2   http://www.mulesource.org/schema/mule/cxf/2.2/mule-cxf.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jms/2.2   http://www.mulesource.org/schema/mule/jms/2.2/mule-jms.xsd\n");
		sb.append("\t\thttp://www.muleforge.org/smooks/schema/mule-module-smooks/1.2 http://dist.muleforge.org/smooks/schema/mule-module-smooks/1.2/mule-module-smooks.xsd\n");
		sb.append("\t\">");

		return sb.toString();
	}

	/**
	 * Generates xsd namespaces for files of type: ${service}--service.xml
	 * @return
	 */
	public String getReqRespTeststubService() {
		StringBuffer sb = new StringBuffer();
		sb.append("\txmlns=\"http://www.mulesource.org/schema/mule/core/2.2\"\n");
		sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("\txmlns:spring=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("\txmlns:jms=\"http://www.mulesource.org/schema/mule/jms/2.2\"\n");
		sb.append("\txsi:schemaLocation=\"\n");
		sb.append("\t\thttp://www.springframework.org/schema/beans     http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/core/2.2  http://www.mulesource.org/schema/mule/core/2.2/mule.xsd\n");
		sb.append("\t\thttp://www.mulesource.org/schema/mule/jms/2.2   http://www.mulesource.org/schema/mule/jms/2.2/mule-jms.xsd\n");
		sb.append("\t\">");

		return sb.toString();
	}
}
