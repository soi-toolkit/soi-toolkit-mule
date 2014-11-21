package org.soitoolkit.commons.mule.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.config.ExceptionHelper;
import org.mule.message.ExceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.mule.api.log.EventLogMessage;
import org.soitoolkit.commons.mule.api.log.EventLogger;
import org.soitoolkit.commons.mule.log.DefaultEventLogger;
import org.soitoolkit.commons.mule.log.EventLoggerFactory;

public class ErrorLogTransformer extends
		org.soitoolkit.commons.mule.log.LogTransformer {
	private static final Logger log = LoggerFactory.getLogger(ErrorLogTransformer.class);

	private EventLogger eventLogger;

	@Override
	public void setMuleContext(MuleContext muleContext) {
		super.setMuleContext(muleContext);

		log.debug("MuleContext injected");
		
		// Also inject the muleContext in the event-logger (since we create the event-logger for now)
		eventLogger = EventLoggerFactory.getEventLogger(muleContext);
		// TODO: this is an ugly workaround for injecting the jaxbObjToXml dependency ...
		if (eventLogger instanceof DefaultEventLogger) {
			if (jaxbContext != null) {
				((DefaultEventLogger) eventLogger).setJaxbContext(jaxbContext);
			}
		}
	}

	/*
	 * Property logLevel 
	 */
	protected LogLevelType logLevel = LogLevelType.ERROR;
	public void setLogLevel(LogLevelType logLevel) {
		this.logLevel = logLevel;
	}

	/*
	 * Property logType 
	 */
	protected String logType = "";
	public void setLogType(String logType) {
		this.logType = logType;
	}

	/*
	 * Property integrationScenario 
	 */
	protected String integrationScenario = "";
	public void setIntegrationScenario(String integrationScenario) {
		this.integrationScenario = integrationScenario;
	}

	/*
	 * Property contractId 
	 */
	protected String contractId = "";
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	/*
	 * Property businessContextId 
	 * 
     * <custom-transformer name="logKivReqIn" class="org.soitoolkit.commons.mule.log.LogTransformer">
	 * 	<spring:property name="logType"  value="Received"/>
	 *    <spring:property name="businessContextId">
	 *      <spring:map>
	 *        <spring:entry key="id1" value="123"/>
	 *        <spring:entry key="id2" value="456"/>
	 *      </spring:map>
	 *    </spring:property>
     * </custom-transformer>
	 * 
	 */
	protected Map<String, String> businessContextId;
	public void setBusinessContextId(Map<String, String> businessContextId) {
		this.businessContextId = businessContextId;
	}

	/*
	 * Property extraInfo 
	 * 
     * <custom-transformer name="logKivReqIn" class="org.soitoolkit.commons.mule.log.LogTransformer">
	 * 	<spring:property name="logType"  value="Received"/>
	 *    <spring:property name="extraInfo">
	 *      <spring:map>
	 *        <spring:entry key="id1" value="123"/>
	 *        <spring:entry key="id2" value="456"/>
	 *      </spring:map>
	 *    </spring:property>
     * </custom-transformer>
	 * 
	 */
	protected Map<String, String> extraInfo;
	public void setExtraInfo(Map<String, String> extraInfo) {
		this.extraInfo = extraInfo;
	}

	public ErrorLogTransformer() {
	}

	private JAXBContext jaxbContext = null;
	/**
	 * Setter for the jaxbContext
	 * 
	 * @param jaxbContext
	 */
	public void setJaxbContext(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

    	try {
    		ExceptionPayload exp = message.getExceptionPayload();
    		if (exp == null) {
    			log.debug("Skip logging message, exception is not detected! ");
    			return message;
    		}

    		Map<String, String> evaluatedExtraInfo         = evaluateMapInfo(extraInfo, message);
    		Map<String, String> evaluatedBusinessContextId = evaluateMapInfo(businessContextId, message);

    		if (log.isDebugEnabled()) {
	    		if (evaluatedBusinessContextId == null) {
	    			log.debug("Null businessContextId");
	    		} else {
	    			Set<Entry<String, String>> es = evaluatedBusinessContextId.entrySet();
	    			for (Entry<String, String> e : es) {
	    				log.debug(e.getKey() + "=" + e.getValue());
					}
	    		}
    		}
    		
    		switch (logLevel) {
    		case WARNING:
			case ERROR:
				EventLogMessage errorLogMsg = new EventLogMessage();
				
				errorLogMsg.setMuleMessage(message);
				errorLogMsg.setLogMessage(logType);
				errorLogMsg.setIntegrationScenario(integrationScenario);
				errorLogMsg.setContractId(contractId);
				errorLogMsg.setBusinessContextId(evaluatedBusinessContextId);
				errorLogMsg.setExtraInfo(evaluatedExtraInfo);
				
				ExceptionPayload exceptionPayload = message.getExceptionPayload();
				Throwable t = null;
				
				if (exceptionPayload != null) {
					t = ExceptionHelper.getRootException(exceptionPayload.getException());
					eventLogger.logErrorEvent(logLevel, t, errorLogMsg);
				} else if (message.getPayload() instanceof ExceptionMessage) {
					ExceptionMessage me = (ExceptionMessage)message.getPayload();
					t = me.getException();
					if (t != null) {
						t = t.getCause();
					}
					eventLogger.logErrorEvent(logLevel, t, errorLogMsg);
				} else {
					String evaluatedLogType = evaluateValue("logType", logType, message);
					eventLogger.logErrorEvent(new RuntimeException(evaluatedLogType), errorLogMsg);
				}
				break;
			}
			return message;
		} catch (Exception e) {
			// be specific about where logging failed, failure might be data-related
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("failed to log event, logType: ");
			errMsg.append(logType);
			errMsg.append(", integrationScenario: ");
			errMsg.append(integrationScenario);
			errMsg.append(", contractId: ");
			errMsg.append(contractId);
			errMsg.append(", businessContextId: ");
			if (businessContextId != null) {
				for (String key : businessContextId.keySet()) {
					errMsg.append("\\n  key: ");
					errMsg.append(key);
					errMsg.append(", value: ");
					errMsg.append(businessContextId.get(key));
				}
			}
			errMsg.append(", extraInfo: ");
			if (extraInfo != null) {
				for (String key : extraInfo.keySet()) {
					errMsg.append("\\n  key: ");
					errMsg.append(key);
					errMsg.append(", value: ");
					errMsg.append(extraInfo.get(key));
				}
			}
			else {
				errMsg.append("null");
			}
						
			log.error(errMsg.toString(), e);
			
			// TODO: should we really re-throw in cases like this where logging have failed? or
			// should we continue and don't let logging fail message flow?
			throw new RuntimeException(errMsg.toString(), e);
		}
    }

	private Map<String, String> evaluateMapInfo(Map<String, String> map, MuleMessage message) {
		
		if (map == null) return null;
		
		Set<Entry<String, String>> ei = map.entrySet();
		Map<String, String> evaluatedMap = new HashMap<String, String>();
		for (Entry<String, String> entry : ei) {
			String key = entry.getKey();
			String value = entry.getValue();
			value = evaluateValue(key, value, message);
		    evaluatedMap.put(key, value);
		}
		return evaluatedMap;
	}

	private String evaluateValue(String key, String value, MuleMessage message) {
		try {
			if(isValidExpression(value)) {
		    	String before = value;
		    	Object eval = muleContext.getExpressionManager().evaluate(value.toString(), message);

		    	if (eval == null) {
		    		value = "UNKNOWN";

		    	} else if (eval instanceof List) {
		    		@SuppressWarnings("rawtypes")
					List l = (List)eval;
		    		value = l.get(0).toString();

		    	} else {
		    		value = eval.toString();
		    	}
		    	if (log.isDebugEnabled()) {
		    		log.debug("Evaluated extra-info for key: " + key + ", " + before + " ==> " + value);
		    	}
		    }
		} catch (Throwable ex) {
			String errMsg = "Faild to evaluate expression: " + key + " = " + value;
			log.warn(errMsg, ex);
			value = errMsg + ", " + ex;
		}
		return value;
	}

	private boolean isValidExpression(String expression) {
		try {
			return muleContext.getExpressionManager().isValidExpression(expression);
		} catch (Throwable ex) {
			return false;
		}
	}
}
