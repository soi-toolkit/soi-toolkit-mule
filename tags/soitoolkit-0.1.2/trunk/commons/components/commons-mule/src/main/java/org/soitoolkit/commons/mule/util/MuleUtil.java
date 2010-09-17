package org.soitoolkit.commons.mule.util;

import org.mule.MuleServer;
import org.mule.api.MuleContext;
import org.mule.config.spring.SpringRegistry;
import org.springframework.context.ApplicationContext;

/**
 * Helper methods for accessing Mule structures
 * 
 * @author Magnus Larsson
 *
 */
public class MuleUtil {

	public static MuleContext getMuleContext () {
	    return MuleServer.getMuleContext();
	}

	public static Object getSpringBean(String beanName) {
		return getSpringBean(getMuleContext(), beanName);
	}

	public static Object getSpringBean(MuleContext muleContext, String beanName) {
	    ApplicationContext ac = (ApplicationContext)muleContext.getRegistry().lookupObject(SpringRegistry.SPRING_APPLICATION_CONTEXT);				
	    return ac.getBean(beanName);
	}	
	
}
