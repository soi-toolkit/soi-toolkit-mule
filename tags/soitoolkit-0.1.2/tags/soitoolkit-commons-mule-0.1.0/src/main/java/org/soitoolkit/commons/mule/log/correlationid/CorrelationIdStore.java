package org.soitoolkit.commons.mule.log.correlationid;

/**
 * Stores correlationId's in a thread local variable.
 * Typically set by a synchronous request-processing and later on picked up by a synchronous response-processing, i.e. executing in the same thread.
 * 
 * TODO: Could a session scoped mule message property be used instead?
 * TODO: Does it have the same scope, i.e. surviving a complete sychronous request and response?
 * 
 * @author Magnus Larsson
 */
public class CorrelationIdStore {
	public static ThreadLocal<String> correlationId = new ThreadLocal<String>();

    /**
     * Hidden constructor.
     */
    private CorrelationIdStore() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }
    
    public static String getCorrelationId() {
    	return correlationId.get();
    }

    public static void setCorrelationId(String newCorrelationId) {
    	correlationId.set(newCorrelationId);
    }
}