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
package org.soitoolkit.tools.generator.plugin.model.impl;

import static org.soitoolkit.tools.generator.plugin.model.impl.ModelUtil.capitalize;
import static org.soitoolkit.tools.generator.plugin.model.impl.ModelUtil.makeJavaName;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.*;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.soitoolkit.tools.generator.plugin.model.IModel;
import org.soitoolkit.tools.generator.plugin.model.ServiceDescriptorModel;
import org.soitoolkit.tools.generator.plugin.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;
import org.soitoolkit.tools.generator.plugin.util.XmlUtil;

public class DefaultModelImpl implements IModel {

	private static final String SOITOOLKIT_VERSION = "0.2.1-SNAPSHOT";

	private String groupId;
	private String artifactId;
	private String version;

	private String service;

	private MuleVersionEnum     muleVersion;
	private List<TransportEnum> transports;
	private TransportEnum inboundTransport;
	private TransportEnum outboundTransport;
	
	private ServiceDescriptorModel serviceDescriptorModel;

	private Map<String, Object> extentions = new HashMap<String, Object>();
	
	private GroovyShell groovyShell;
	
	/**
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param service
	 * @param transports
	 * @param serviceDescriptor
	 * @param operations
	 */
	public void initModel(String groupId, String artifactId, String version, String service, MuleVersionEnum muleVersion, List<TransportEnum> transports, TransportEnum inboundTransport, TransportEnum outboundTransport, String serviceDescriptor, List<String> operations) {

		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.service = service;
		this.muleVersion = muleVersion;
		this.transports = transports;
		this.inboundTransport = inboundTransport;
		this.outboundTransport = outboundTransport;

		serviceDescriptorModel = (serviceDescriptor == null) ? null : new ServiceDescriptorModel(this, serviceDescriptor, operations);

		groovy.lang.Binding binding = new Binding();
		binding.setVariable("m", this);
    	groovyShell = new GroovyShell(getClass().getClassLoader(), binding);
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#resolveParameter(java.lang.String)
	 */
	public Object resolveParameter(String parameterName) {
		return resolveParameter(parameterName, parameterName);
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#resolveParameter(java.lang.String, java.lang.Object)
	 */
	public Object resolveParameter(String parameterName, Object defaultValue) {
		try {
			return groovyShell.evaluate("m." + parameterName);
		} catch (RuntimeException rt) {
			// TODO: Debug warning here...
			return defaultValue;
		}
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getSd()
	 */
	public ServiceDescriptorModel getSd() {
		return serviceDescriptorModel;
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getExt()
	 */
	public Map<String, Object> getExt() {
		return extentions;
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getDollarSymbol()
	 */
	public String getDollarSymbol() {
		return "$";
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getXmlTimestamp()
	 */
	public String getXmlTimestamp() {
		return XmlUtil.convertDateToXmlDate(new Date()).toString();
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getGroupId()
	 */
	public String getGroupId() {
		return groupId;
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getArtifactId()
	 */
	public String getArtifactId() {
		return artifactId;
	}
		
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getCapitalizedArtifactId()
	 */
	public String getCapitalizedArtifactId() {
		return capitalize(getArtifactId());
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getLowercaseArtifactId()
	 */
	public String getLowercaseArtifactId() {
		return getArtifactId().toLowerCase();
	}
	
	public String getCapitalizedJavaArtifactId() {
		return capitalize(getJavaArtifactId());
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getVersion()
	 */
	public String getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getService()
	 */
	public String getService() {
		return service;
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getCapitalizedService()
	 */
	public String getCapitalizedService() {
		return capitalize(getService());
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getLowercaseService()
	 */
	public String getLowercaseService() {
		return getService().toLowerCase();
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getUppercaseService()
	 */
	public String getUppercaseService() {
		return getService().toUpperCase();
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getCapitalizedJavaService()
	 */
	public String getCapitalizedJavaService() {
		return capitalize(getJavaService());
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getLowercaseService()
	 */
	public String getLowercaseJavaService() {
		return getJavaService().toLowerCase();
	}
	
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getSoitoolkitVersion()
	 */
	public String getSoitoolkitVersion() {
		return SOITOOLKIT_VERSION;
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getSuperpomGroupId()
	 */
	public String getSuperpomGroupId() {
		return "org.soitoolkit.commons.poms";
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getSuperpomArtifactId()
	 */
	public String getSuperpomArtifactId() {
		return "soitoolkit-default-parent";
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getSuperpomVersion()
	 */
	public String getSuperpomVersion() {
		return getSoitoolkitVersion();
	}
	

	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getParentPom()
	 */
	public String getParentPom() {
		return getArtifactId();
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getIntegrationComponentProject()
	 */
	public String getIntegrationComponentProject() {
		return getArtifactId();
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getServiceProject()
	 */
	public String getServiceProject() {
		return getArtifactId() + "-services";
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getServiceProjectFilePath()
	 */
	@Override
	public String getServiceProjectFilepath() {
		return getServiceProject();
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getSchemaProject()
	 */
	public String getSchemaProject() {
		return getArtifactId() + "-schemas";
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getSchemaProject()
	 */
	public String getSchemaProjectFilepath() {
		return getSchemaProject();
	}
	
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getWebProject()
	 */
	public String getWebProject() {
		return getArtifactId() + "-web";
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getWebProjectFilepath()
	 */
	@Override
	public String getWebProjectFilepath() {
		return getWebProject();
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getTeststubWebProject()
	 */
	public String getTeststubWebProject() {
		return getArtifactId() + "-teststub-web";
	}
	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getTeststubWebProjectFilepath()
	 */
	@Override
	public String getTeststubWebProjectFilepath() {
		return getTeststubWebProject();
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getJavaPackage()
	 */
	public String getJavaPackage() {
		String javaPackage = getJavaGroupId(); 
		if (!isGroupIdSuffixedWithArtifactId()) {
			javaPackage += "." + getJavaArtifactId(); 
		}
		return javaPackage.toLowerCase();
	}

	/* (non-Javadoc)
	 * @see org.soitoolkit.tools.generator.plugin.model.IModel#getJavaPackageFilepath()
	 */
	public String getJavaPackageFilepath() {
		return getJavaPackage().replace('.', '/');
	}

    public String getDefaultSftpIdentityFile() {
    	return PreferencesUtil.getDefaultSftpIdentityFile();
    }
    public String getDefaultSftpIdentityPassphrase() {
    	return PreferencesUtil.getDefaultSftpIdentityPassphrase();
    }

    // JMS Naming...

    public String getJmsInQueue() {
    	return getJavaArtifactId().toUpperCase() + "." + getService().toUpperCase() + ".IN.QUEUE"; 
    }
    public String getJmsOutQueue() {
    	return getJavaArtifactId().toUpperCase() + "." + getService().toUpperCase() + ".OUT.QUEUE"; 
    }
    public String getJmsDLQueue() {
    	return "DLQ." + getJmsInQueue(); 
    }
    public String getJmsLogInfoQueue() {
    	return "SOITOOLKIT.LOG.INFO"; 
    }
    public String getJmsLogErrorQueue() {
    	return "SOITOOLKIT.LOG.ERROR"; 
    }

    // Mule version
    public String getMuleVersion() {
    	return muleVersion.getPomSuffix();
    }

    // Transports
	@Override
	public boolean isSftp() {
		return isTransportSelected(TransportEnum.SFTP);
	}

	@Override
	public boolean isJms() {
		return isTransportSelected(TransportEnum.JMS);
	}	

	@Override
	public boolean isJdbc() {
		return isTransportSelected(TransportEnum.JDBC);
	}	

	@Override
	public boolean isServlet() {
		return isTransportSelected(TransportEnum.SERVLET);
	}	

	@Override
    public boolean isPop3() {
		return isTransportSelected(TransportEnum.POP3);
	}	

	@Override
    public boolean isImap() {
		return isTransportSelected(TransportEnum.IMAP);
	}	

	@Override
    public boolean isSmtp() {
		return isTransportSelected(TransportEnum.SMTP);
	}	


    public String getInboundTransport() {
    	return inboundTransport.name();
    }
    public String getOutboundTransport() {
    	return outboundTransport.name();
    }

    
    public boolean isServiceTransactional() {
    	return isInboundTransportTransactional() && isOutboundTransportTransactional();
    }
    			    	
    public boolean isServiceXaTransactional() {
    	return false; // Wait a bit more for this one :-)
    }
    public boolean isInboundEndpointFilebased() {
    	return inboundTransport == FILE ||
    	       inboundTransport == FTP  ||
    	       inboundTransport == SFTP;
    }
    public boolean isOutboundEndpointFilebased() {
    	return outboundTransport == FILE ||
    	       outboundTransport == FTP  ||
    	       outboundTransport == SFTP;
    }

    
    // Property files
    public String getConfigPropertyFile() {
    	return getArtifactId() + "-config";
    }
	public String getSecurityPropertyFile() {
    	return getArtifactId() + "-security";
    }


	// --------------------	

	protected boolean isInboundTransportTransactional() {
		return inboundTransport==JMS || inboundTransport==JDBC;
	}
	protected boolean isOutboundTransportTransactional() {
		return outboundTransport==JMS || outboundTransport==JDBC;
	}


	protected boolean isTransportSelected(TransportEnum selectedTransport) {

		if (transports == null) return false;

		for (TransportEnum transport : transports) {
			if (transport == selectedTransport) return true;
		}
		return false;
	}

	protected String getJavaGroupId() {
		return makeJavaName(getGroupId());
	}
	
	protected String getJavaArtifactId() {
		return makeJavaName(getArtifactId());
	}

	protected String getJavaService() {
		return makeJavaName(getService());
	}

	/**
	 * Note: Keep the method public in its package so that is can be tested properly
	 * @return
	 */
	boolean isGroupIdSuffixedWithArtifactId() {
		String grpId = getJavaGroupId();
		String artId = getJavaArtifactId();
		
		// Return false if one of them are null...
		if (grpId == null || artId == null) {
			return false;
		}
		
		int grpIdLen = grpId.length();
		int artIdLen = artId.length();

		// Return false if artId is longer then grpId
		if (artIdLen > grpIdLen) {
			return false;
		}
		
		grpId = grpId.toLowerCase();
		artId = artId.toLowerCase();
		
		int artIdInGrpIdLastPosition = grpId.lastIndexOf(artId);

		// Return false if artId is not found in grpId
		if (artIdInGrpIdLastPosition == -1) {
			return false;
		}

		// Return false if the last position of artId is not in the end of the grpId
		if (grpIdLen != artIdInGrpIdLastPosition + artIdLen) {
			return false;
		}
		
		// Ok, the grpId is really suffixed with the artifactId, let's return true :-)
		return true;
	}
}
