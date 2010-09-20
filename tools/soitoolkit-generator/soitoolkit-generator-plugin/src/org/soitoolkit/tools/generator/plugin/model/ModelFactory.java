package org.soitoolkit.tools.generator.plugin.model;

import java.util.List;

import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;

public class ModelFactory {

	private static Class<DefaultModelImpl> modelClass = DefaultModelImpl.class;

	/**
     * Hidden constructor.
     */
    private ModelFactory() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    /**
     * Register a custom model class, must be a subclass to DefaultModelImpl.
     * 
     * @param modelClass
     * @throws IllegalArgumentException if supplied class is not a subclass of DefaultModelImpl
     */
    @SuppressWarnings("unchecked")
	public static void setModelClass(Class<?> modelClass) throws IllegalArgumentException {
    	if (!DefaultModelImpl.class.isAssignableFrom(modelClass)) {
    		throw new IllegalArgumentException("Modelclass, " + modelClass.getName() + ", is not a subtype of " + DefaultModelImpl.class.getName());
    	}
		ModelFactory.modelClass = (Class<DefaultModelImpl>)modelClass;
    	System.err.println("### Set model-class: " + ModelFactory.modelClass.getName());
	}

    /**
	 * Constructor-method to use when service descriptors are not required (e.g. schema and wsdl for services)
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param service
     * @return the new model instance
	 */
    public static IModel newModel(String groupId, String artifactId, String version, String service, List<TransportEnum> transports) {
		return newModel(groupId, artifactId, version, service, transports, null, null);
	}

	/**
	 * Constructor-method to use when service descriptors are required (e.g. schema and wsdl for services)
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param service
	 * @param serviceDescriptor
	 * @param operations
     * @return the new model instance
     */
    public static IModel newModel(String groupId, String artifactId, String version, String service, List<TransportEnum> transports, String serviceDescriptor, List<String> operations) {
		try {
	    	DefaultModelImpl m = (DefaultModelImpl)modelClass.newInstance();
	    	m.initModel(groupId, artifactId, version, service, transports, serviceDescriptor, operations);
	    	System.err.println("### New model-class: " + m.getClass().getName());
	    	return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

}
