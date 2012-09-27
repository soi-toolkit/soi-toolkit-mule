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
package org.soitoolkit.commons.studio.components.logger;

import java.util.Map;

import javax.inject.Inject;

import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.param.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.studio.components.logger.api.EventLogger;
import org.soitoolkit.commons.studio.components.logger.impl.DefaultEventLogger;
import org.springframework.stereotype.Component;

/**
 * soi-toolkit module for logging
 * 
 * @author Magnus Larsson
 */
@SuppressWarnings("deprecation")
@Component
@Module(name="st-logger", schemaVersion="0.5.1-SNAPSHOT")
public class LoggerModule {

	/*
	 * TODO:
	 * - kolla av genererat xsd namespace, 0.5.1-SNAPSHOT borde vara current!!!
	 * - få igång mot st utan extern deps
	 * - kolla av parametrar som används i vgr, t ex e-handel...
	 * - hur få in soitoolkit som namn i pluginen så att det inte bara står "Mule Cloud Connector Mule Studio Extension" i mule studio 
	 * - ersätt standard apache licens med vår egen!
	 * - updat mvn-enforcer till 1.1.1 * 2 pga http://jira.codehaus.org/browse/MENFORCER-117 och module-logger 
	 * - ta bort beroende till slf4j och se till att den (3 jar-filer) inte paketeras med i zip-fil eller update-site
	 * - ta bort beroende till commons-mule, kopiera in kod helt enkelt...
	 * - ta bort pa
	 * + separata metoder för varje loggnivå
	 * + bort med mule-module-rest 1.0 och dep till devkit 3.0.2 
	 * - DI fallback i getters måste vara att default impl instansieras...
	 * - classpath scanning måste med i alla projekt...
	 * - dekl av defaulteventlogger måste oxå med???
	 * - impl detaljer...
	 *   - få bort håtdkodningar i form av könamn och connectorer, flytta ut till config-element med bra defaultväden, typ properties i property fil...
	 * + busContext, behövs det eller räcker det med extraInfo + correlationId
	 * - behövs busCorrId som namn eller kan vi bara kalla det corrId och defaulta det till mule's?
	 * - bättre namn i mule studio
	 * - ikoner...
	 * - explicita set*Beans på config-element?
	 *
	 */

	private static final Logger logger = LoggerFactory.getLogger(LoggerModule.class);

	/**
	 * Optional name of the integration scenario or business process
     */
	@Optional @Configurable
    private String integrationScenario = null;

	/**
     * Set property
	 * 
	 * @param integrationScenario Integration scenario
	 */
	public void setIntegrationScenario(String integrationScenario)
    {
		System.err.println("### GOT STATIC IS: " + integrationScenario);
        this.integrationScenario = integrationScenario;
    }

	/**
     * Get property
     */
    public String getIntegrationScenario()
    {
        return integrationScenario;
    }


	/**
	 *  Optional extra info
     */
	@Optional @Configurable
    private Map<String, String> extraInfo = null;

	/**
     * Set property
	 * 
	 * @param extraInfo Extra info
	 */
	public void setExtraInfo(Map<String, String> extraInfo)
    {
		System.err.println("### GOT STATIC EI: " + extraInfo);
        this.extraInfo = extraInfo;
    }
	
	/**
     * Get property
     */
    public Map<String, String> getExtraInfo()
    {
        return extraInfo;
    }
	
    
	/**
	 * Optional custom implementation class of the event-logger
     */
	@Optional @Configurable
    private String customEventLoggerImpl = null;
	
	private EventLogger customEventLogger = null;

	/**
     * Set property
	 * 
	 * @param customEventLoggerImpl custom implementation class of the event-logger
	 */
	public void setCustomEventLoggerImpl(String customEventLoggerImpl)
    {
		System.err.println("### GOT CustomEventLoggerImpl: " + customEventLoggerImpl);
        this.customEventLoggerImpl = customEventLoggerImpl;
        try {
        	System.err.println("Try to load class: " + customEventLoggerImpl);
        	Class c = this.getClass().getClassLoader().loadClass(customEventLoggerImpl);
        	Object o = c.newInstance();
        	System.err.println("Load an object of class: " + o.getClass().getName());
        	customEventLogger = (EventLogger)o;
        	System.err.println("Load of a custom EventLogger done");
        } catch (Throwable ex) {
        	
        }
    }

	/**
     * Get property
     */
    public String getCustomEventLoggerImpl()
    {
        return customEventLoggerImpl;
    }


	
    /*
     * Dependencies
     */
    private EventLogger eventLogger;

    @Inject
    public void setEventLogger(EventLogger eventLogger) {
    	this.eventLogger = eventLogger;
    }

    protected EventLogger getEventLogger() {

    	// Let a custom EventLogger have preference over the standard impl.
    	if (customEventLogger != null) {
    		return customEventLogger;
    	}
    	
    	if (eventLogger == null) {
    		// Fallback if classpath-scanning is missing, eg: <context:component-scan base-package="org.soitoolkit.commons.module.logger" />
    		eventLogger = new DefaultEventLogger();
    	}
    	return eventLogger;
    }

    /**
     * Log processor for level INFO
     *
     * {@sample.xml ../../../doc/soitoolkit-connector.xml.sample soitoolkit:log-info}
     *
     * @param message Log-message to be processed
	 * @param integrationScenario Optional name of the integration scenario or business process
	 * @param messageType Optional name of the message type, e.g. a XML Schema namespace for a XML payload
	 * @param contractId Optional name of the contract in use
	 * @param correlationId Optional correlation identity of the message
     * @param extraInfo Optional extra info
     * @return The incoming payload
     */ 
    @Processor
    public Object logInfo(
    	@Optional @FriendlyName("Log Message") String message, 
    	@Optional String integrationScenario, 
    	@Optional String messageType,
    	@Optional String contractId,
    	@Optional String correlationId,
    	@Optional @FriendlyName("Extra Info") Map<String, String> extraInfo) {

    	return doLog(LogLevelType.INFO, message, integrationScenario, contractId, correlationId, extraInfo);
    }

    /**
     * Log processor for level WARNING
     *
     * {@sample.xml ../../../doc/soitoolkit-connector.xml.sample soitoolkit:log-warning}
     *
     * @param message Log-message to be processed
	 * @param integrationScenario Optional name of the integration scenario or business process
	 * @param messageType Optional name of the message type, e.g. a XML Schema namespace for a XML payload
	 * @param contractId Optional name of the contract in use
	 * @param correlationId Optional correlation identity of the message
     * @param extraInfo Optional extra info
     * @return The incoming payload
     */ 
    @Processor
    public Object logWarning(
    	@Optional @FriendlyName("Log Message") String message, 
    	@Optional String integrationScenario, 
    	@Optional String messageType,
    	@Optional String contractId,
    	@Optional String correlationId,
    	@Optional @FriendlyName("Extra Info") Map<String, String> extraInfo) {

    	return doLog(LogLevelType.WARNING, message, integrationScenario, contractId, correlationId, extraInfo);
    }

    /**
     * Log processor for level ERROR
     *
     * {@sample.xml ../../../doc/soitoolkit-connector.xml.sample soitoolkit:log-error}
     *
     * @param message Log-message to be processed
	 * @param integrationScenario Optional name of the integration scenario or business process
	 * @param messageType Optional name of the message type, e.g. a XML Schema namespace for a XML payload
	 * @param contractId Optional name of the contract in use
	 * @param correlationId Optional correlation identity of the message
     * @param extraInfo Optional extra info
     * @return The incoming payload
     */ 
    @Processor
    public Object logError(
    	@Optional @FriendlyName("Log Message") String message, 
    	@Optional String integrationScenario, 
    	@Optional String messageType,
    	@Optional String contractId,
    	@Optional String correlationId,
    	@Optional @FriendlyName("Extra Info") Map<String, String> extraInfo) {

    	return doLog(LogLevelType.ERROR, message, integrationScenario, contractId, correlationId, extraInfo);
    }

    /**
     * Log processor for level DEBUG
     *
     * {@sample.xml ../../../doc/soitoolkit-connector.xml.sample soitoolkit:log-debug}
     *
     * @param message Log-message to be processed
	 * @param integrationScenario Optional name of the integration scenario or business process
	 * @param messageType Optional name of the message type, e.g. a XML Schema namespace for a XML payload
	 * @param contractId Optional name of the contract in use
	 * @param correlationId Optional correlation identity of the message
     * @param extraInfo Optional extra info
     * @return The incoming payload
     */ 
    @Processor
    public Object logDebug(
    	@Optional @FriendlyName("Log Message") String message, 
    	@Optional String integrationScenario, 
    	@Optional String messageType,
    	@Optional String contractId,
    	@Optional String correlationId,
    	@Optional @FriendlyName("Extra Info") Map<String, String> extraInfo) {

    	return doLog(LogLevelType.DEBUG, message, integrationScenario, contractId, correlationId, extraInfo);
    }

    /**
     * Log processor for level TRACE
     *
     * {@sample.xml ../../../doc/soitoolkit-connector.xml.sample soitoolkit:log-trace}
     *
     * @param message Log-message to be processed
	 * @param integrationScenario Optional name of the integration scenario or business process
	 * @param messageType Optional name of the message type, e.g. a XML Schema namespace for a XML payload
	 * @param contractId Optional name of the contract in use
	 * @param correlationId Optional correlation identity of the message
     * @param extraInfo Optional extra info
     * @return The incoming payload
     */ 
    @Processor
    public Object logTrace(
    	@Optional @FriendlyName("Log Message") String message, 
    	@Optional String integrationScenario, 
    	@Optional String messageType,
    	@Optional String contractId,
    	@Optional String correlationId,
    	@Optional @FriendlyName("Extra Info") Map<String, String> extraInfo) {

    	return doLog(LogLevelType.TRACE, message, integrationScenario, contractId, correlationId, extraInfo);
    }

    protected Object doLog(
    	LogLevelType level,
    	String message, 
    	String integrationScenario, 
    	String contractId, 
    	String correlationId,
    	Map<String, String> extraInfo) {

    	if (integrationScenario == null && this.integrationScenario != null) {
    		integrationScenario = this.integrationScenario;
    	}

    	if (this.extraInfo != null) {
        	if (extraInfo == null) {
        		extraInfo = this.extraInfo;
        	} else {
        		// TODO. Verify that values in the current extrainfo wins over the extrainf in the configuration. 
        		extraInfo.putAll(this.extraInfo);
        	}
    	}
    	
    	// Get the MuleEvent from the RequestContent instead of having payload and headers injected in method call.
    	// Injecting the payload will cause an evaluation of the expression [#payload] on every call, so it will be a performance killer...
    	// MuleEvent also includes a lot of information that we can't get injected
		MuleEvent muleEvent = RequestContext.getEvent();

    	getEventLogger().logEvent(muleEvent, message, level, integrationScenario, contractId, correlationId, extraInfo);

    	return muleEvent.getMessage().getPayload();
    }
}