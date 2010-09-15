package org.soitoolkit.commons.mule.error;

import java.util.Map;

import org.mule.DefaultExceptionStrategy;
import org.mule.api.MessagingException;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.config.ExceptionHelper;
import org.soitoolkit.commons.mule.log.EventLogger;

/**
 * Base exception handler that catch errors and log them using the event-logger.
 * 
 * @author Magnus Larsson
 *
 */
public class ExceptionHandler extends DefaultExceptionStrategy {

	private static final EventLogger eventLogger = new EventLogger();

	@SuppressWarnings("unchecked")
	@Override
	protected void logException(Throwable t) {
//		No need to double log this type of errors
//		super.logException(t);

        MuleException muleException = ExceptionHelper.getRootMuleException(t);
        if (muleException != null)
        {
        	if (muleException instanceof MessagingException) {
        		MessagingException me = (MessagingException)muleException;
            	eventLogger.logErrorEvent(muleException, me.getMuleMessage());

        	} else {
                Map<String, Object> info = ExceptionHelper.getExceptionInfo(muleException);
            	eventLogger.logErrorEvent(muleException, info.get("Payload"));
        	}
        	
        } else {
        	eventLogger.logErrorEvent(t, (Object)null);
        }
	}

	@Override
	protected void logFatal(MuleMessage message, Throwable t) {
//		This type of fatal error (i.e. problem with the error handling itself) is best to log both with Mule's standard error-logging and our own
		super.logFatal(message, t);

		eventLogger.logErrorEvent(t, message);
	}
}