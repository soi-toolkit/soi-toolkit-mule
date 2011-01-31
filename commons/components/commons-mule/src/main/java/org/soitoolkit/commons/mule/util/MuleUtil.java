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
package org.soitoolkit.commons.mule.util;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.service.Service;
import org.mule.config.spring.SpringRegistry;
import org.mule.context.notification.EndpointMessageNotification;
import org.springframework.context.ApplicationContext;

/**
 * Helper methods for accessing Mule structures
 * 
 * @author Magnus Larsson
 *
 */
public class MuleUtil {

	/**
     * Hidden constructor.
     */
    private MuleUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    public static Object getSpringBean(MuleContext muleContext, String beanName) {
	    ApplicationContext ac = (ApplicationContext)muleContext.getRegistry().lookupObject(SpringRegistry.SPRING_APPLICATION_CONTEXT);				
	    return ac.getBean(beanName);
	}	

	/**
	 * Different implementations for retrieving the service name from a MuleEventContext-object in Mule 2.2.x and Mule 
	 * 
	 * @param event
	 * @return
	 */
	public static String getServiceName(MuleEventContext event) {
        // Mule 2.2 implementation
		Service    service = (event == null)? null : event.getService();
        // FlowConstruct service = (event == null)? null : event.getFlowConstruct();
        String        name    = (service == null)?  "" : service.getName();
        return name;
	}

	/**
	 * Different implementations for creating a MuleMessage in Mule 2.2.x and Mule 
	 * 
	 * @param message
	 * @param muleContext
	 * @return
	 */
	public static MuleMessage createMuleMessage(Object message, MuleContext muleContext) {
        // Mule 2.2 implementation
		return new DefaultMuleMessage(message);
		// return new DefaultMuleMessage(message, muleContext);
	}

	/**
	 * Different implementations for retrieving the endpoint name from a EndpointMessageNotification-object in Mule 2.2.x and Mule 
	 * 
	 * @param notification
	 * @return
	 */
	public static String getEndpointName(EndpointMessageNotification notification) {
        // Mule 2.2 implementation
		return notification.getEndpoint().getName();
		// return notification.getEndpoint();
	}
	
}