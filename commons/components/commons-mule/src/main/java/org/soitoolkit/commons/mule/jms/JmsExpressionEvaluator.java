package org.soitoolkit.commons.mule.jms;

import javax.jms.Message;

import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionEvaluator;
import org.mule.api.transport.PropertyScope;
import org.mule.transport.jms.JmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsExpressionEvaluator implements ExpressionEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsExpressionEvaluator.class);

    protected String evaluatorName = "jms";

    public static final String EXPR_IS_EXHAUSTED = "isExhausted";

    public static final String EXPR_IS_REDELIVERY = "isRedelivery";

    protected int maxRedeliveries;

    /*
     * (non-Javadoc)
     * @see org.mule.api.expression.ExpressionEvaluator#evaluate(java.lang.String, org.mule.api.MuleMessage)
     */
    @Override
    public Object evaluate(String expression, MuleMessage message) {
        String object = null;

        Integer deliveryCount;
        String jmsMessageId;

        if (message.getOriginalPayload() != null && message.getOriginalPayload() instanceof Message) {
            try {
                deliveryCount =
                    ((Message) message.getOriginalPayload()).getIntProperty(JmsConstants.JMS_X_DELIVERY_COUNT);
                jmsMessageId = ((Message) message.getOriginalPayload()).getStringProperty(JmsConstants.JMS_MESSAGE_ID);
            } catch (Exception e) {
                throw new RuntimeException("Failed to evaluate JMS message. Expression evaluator: " + expression, e);
            }
        } else {
            deliveryCount = message.getProperty(JmsConstants.JMS_X_DELIVERY_COUNT, PropertyScope.INVOCATION);
            jmsMessageId = message.getProperty(JmsConstants.JMS_MESSAGE_ID, PropertyScope.INVOCATION);
            if (deliveryCount == null || jmsMessageId == null) {
                throw new RuntimeException("Failed to evaluate JMS message. Expression evaluator: " + expression);
            }
        }

        LOGGER.debug("JMSMessageID: {}, JMSXDeliveryCount: {}", jmsMessageId, deliveryCount);

        if (EXPR_IS_EXHAUSTED.equals(expression)) {

            if (maxRedeliveries < deliveryCount) {
                LOGGER.warn("Exhausted! JMSMessageID: {}, JMSXDeliveryCount: {}", jmsMessageId, deliveryCount);
                return true;
            }
            return false;
        } else if (EXPR_IS_REDELIVERY.equals(expression)) {

            if (deliveryCount == 1) {
                return false;
            }
            return true;
        }
        return object;
    }

    public int getMaxRedeliveries() {
        return maxRedeliveries;
    }

    public void setMaxRedeliveries(int maxRedeliveries) {
        this.maxRedeliveries = maxRedeliveries;
    }

    @Override
    public String getName() {
        return evaluatorName;
    }

    public void setName(String name) {
        this.evaluatorName = name;
    }
}
