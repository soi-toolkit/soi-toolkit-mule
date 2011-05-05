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
package org.soitoolkit.commons.mule.error;

import java.util.Map;

import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.config.ExceptionHelper;
import org.soitoolkit.commons.mule.api.log.EventLogMessage;
import org.soitoolkit.commons.mule.api.log.EventLogger;
import org.soitoolkit.commons.mule.log.EventLoggerFactory;

/**
 * FIXME: Needs to be reimplemented for Mule 3.1's MessagingExceptionHandler and SystemExceptionHandler-interface
 * 
 * Base exception handler that catch errors and log them using the event-logger.
 * 
 * @author Magnus Larsson
 *
 */
public class ExceptionHandler implements MuleContextAware { // extends DefaultExceptionStrategy {

	private EventLogger eventLogger;
	
	private MuleContext muleContext;
	public void setMuleContext(MuleContext muleContext) {
		this.muleContext = muleContext;
		eventLogger = EventLoggerFactory.getEventLogger(muleContext);
	}	

	@SuppressWarnings("unchecked")
//	@Override
	protected void logException(Throwable t) {
//		No need to double log this type of errors
//		super.logException(t);

		// Inject the MuleContext in the EventLogger since we are creating the instance
//		eventLogger.setMuleContext(muleContext);
		
        MuleException muleException = ExceptionHelper.getRootMuleException(t);
        if (muleException != null)
        {
        	if (muleException instanceof MessagingException) {
        		MessagingException me = (MessagingException)muleException;
            	//eventLogger.logErrorEvent(muleException, me.getMuleMessage(), null, null);
        		EventLogMessage elm = new EventLogMessage();
        		elm.setMuleMessage(me.getMuleMessage());
        		eventLogger.logErrorEvent(muleException, elm);
        	} else {
                Map<String, Object> info = ExceptionHelper.getExceptionInfo(muleException);
            	//eventLogger.logErrorEvent(muleException, info.get("Payload"), null, null);
        		EventLogMessage elm = new EventLogMessage();
        		//elm.setMuleMessage(message);
        		eventLogger.logErrorEvent(muleException, info.get("Payload"), elm);
        	}
        	
        } else {
        	//eventLogger.logErrorEvent(t, (Object)null, null, null);
    		EventLogMessage elm = new EventLogMessage();
    		//elm.setMuleMessage(message);	
    		eventLogger.logErrorEvent(t, elm);        	
        }
	}

//	@Override
	protected void logFatal(MuleMessage message, Throwable t) {
//		This type of fatal error (i.e. problem with the error handling itself) is best to log both with Mule's standard error-logging and our own
//		super.logFatal(message, t);

		//eventLogger.logErrorEvent(t, message, null, null);
		EventLogMessage elm = new EventLogMessage();
		elm.setMuleMessage(message);		
		eventLogger.logErrorEvent(t, elm);		
	}

}