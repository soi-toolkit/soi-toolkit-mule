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
package org.soitoolkit.commons.mule.log;

import org.mule.api.MuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.api.log.EventLogger;

/**
 * Factory to produce either a default or a configured custom implementation of
 * an EventLogger.
 * <p>
 * This is a temporary solution (since we don't yet wire up these beans using
 * the Spring-context) for:
 * http://code.google.com/p/soi-toolkit/issues/detail?id=41
 * <p>
 * The produced EventLogger will not be a singleton due to the fact that it must
 * currently be injected with properties by the factory (instead of by Spring)
 * and therefore ultimately by the class using the factory.
 * <p>
 * A custom EventLogger should be declared an integration components
 * service-project src/main/resources/${artifactId}-common.xml like below.
 * <b>Note the primary="true" which will allow us to autowire byName to override
 * the DefaultEventLogger when we expose it as a Spring bean</b>
 * 
 * <pre>
 * &lt;spring:beans&gt;
 *   &lt;spring:bean name="soitoolkit.eventLogger" class="org.sample.issue41st041snap.issue41.CustomEventLogger" primary="true"/&gt;
 * &lt;/spring:beans&gt;
 * </pre>
 * 
 * @author hakan
 */
public class EventLoggerFactory {
	private static final String INIT_ERROR_MISSING_MULECONTEXT = "muleContext is not injected (is null)";
	private static final String CUSTOM_EVENT_LOGGER_BEAN_NAME = "soitoolkit.eventLogger";
	private static final EventLoggerFactory factoryInstance = new EventLoggerFactory();
	private Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * <b>Caution:</b> there is a possibility that a custom EventLogger isn't
	 * yet instantiated by Spring (and thus not yet availbale in the context)
	 * when we try to look it up, in such a case we could end up with the wrong
	 * EventLogger in some parties that depend on the EventLogger.
	 * 
	 * @param muleContext
	 *            Must supply the MuleContext since our DefaultEventLogger
	 *            doesn't get injected with MuleContext (due to the fact that it
	 *            isn't exposed as a Spring bean in the context yet). The
	 *            MuleContext will however not be injected into a custom
	 *            EventLogger bean, it will have to implement MuleContextAware
	 *            and let Mule inject the context.
	 * @return A unique DefaultEventLogger instance (see header for reasoning)
	 *         or a custom EventLogger.
	 */
	public static EventLogger getEventLogger(MuleContext muleContext) {
		return factoryInstance.getEventLoggerImpl(muleContext);
	}

	private EventLogger getEventLoggerImpl(MuleContext muleContext) {
		assertNotNull(INIT_ERROR_MISSING_MULECONTEXT, muleContext);
		EventLogger el = getCustomEventLogger(muleContext);
		if (el != null) {
			log.debug("returning custom eventlogger type: {}", el.getClass()
					.getName());
			return el;
		}
		log.debug("returning default eventlogger");
		return getDefaultEventLogger(muleContext);
	}

	private EventLogger getDefaultEventLogger(MuleContext muleContext) {
		DefaultEventLogger el = new DefaultEventLogger();
		// we inject the muleContext here since we use new ...
		el.setMuleContext(muleContext);
		return el;
	}

	// TODO: how can we know that the custom bean is created when this method is
	// invoked?
	private EventLogger getCustomEventLogger(MuleContext muleContext) {
		Object obj = getCustomEventLoggerFromRegistry(muleContext);
		if (obj == null) {
			log.debug("no custom event logger defined using bean name: {}",
					CUSTOM_EVENT_LOGGER_BEAN_NAME);
			return null;
		}
		if (obj instanceof EventLogger) {
			log.debug(
					"custom event logger defined using bean name: {}, impl class: {}",
					CUSTOM_EVENT_LOGGER_BEAN_NAME, obj.getClass().getName());
			return (EventLogger) obj;
		} else {
			String errMsg = "custom event logger defined using bean name: "
					+ CUSTOM_EVENT_LOGGER_BEAN_NAME
					+ " does not implement required interface: "
					+ EventLogger.class.getName() + ", impl class: "
					+ obj.getClass().getName();
			log.error(errMsg);
			throw new IllegalStateException(errMsg);
		}
	}

	// open up for testing without a live MuleContext
	protected Object getCustomEventLoggerFromRegistry(MuleContext muleContext) {
		Object obj = muleContext.getRegistry().lookupObject(
				CUSTOM_EVENT_LOGGER_BEAN_NAME);
		return obj;
	}

	private void assertNotNull(String assertMessage, Object testee) {
		if (testee == null) {
			throw new IllegalStateException(assertMessage);
		}
	}

}
