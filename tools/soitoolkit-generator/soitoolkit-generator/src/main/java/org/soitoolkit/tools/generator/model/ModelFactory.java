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
package org.soitoolkit.tools.generator.model;

import static org.soitoolkit.tools.generator.util.MiscUtil.convertStreamToString;
import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.model.impl.DefaultModelImpl;

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
	}

    /**
     * Register a custom model groovy class, must be a subclass to DefaultModelImpl.
     * 
     * @param modelClass
     * @throws IllegalArgumentException if supplied class is not a subclass of DefaultModelImpl
     * @throws IOException 
     */
	@SuppressWarnings("rawtypes")
	public static void setModelGroovyClass(URL url) throws IllegalArgumentException, IOException {
    	String            groovyCode  = convertStreamToString(url.openStream());
    	ClassLoader       parent      = ModelFactory.class.getClassLoader();
    	GroovyClassLoader loader      = new GroovyClassLoader(parent);
    	Class             groovyClass = loader.parseClass(groovyCode);

    	setModelClass(groovyClass);
	}


	/**
     * Reset model class to the default class.
     * 
     * @param modelClass
     * @throws IllegalArgumentException if supplied class is not a subclass of DefaultModelImpl
     */
	public static void resetModelClass() {
		ModelFactory.modelClass = DefaultModelImpl.class;
    	System.err.println("[INFO] Reset model-class: " + ModelFactory.modelClass.getName());
	}

    /**
	 * Constructor-method to use when services with inbound and outbound services
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param service
     * @return the new model instance
	 */
    public static IModel newModel(String groupId, String artifactId, String version, String service, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType) {
		return doCreateNewModel(groupId, artifactId, version, service, muleVersion, null, null, inboundTransport, outboundTransport, transformerType, null, null);
	}

    /**
	 * Constructor-method to use when service descriptors are not required (e.g. schema and wsdl for services)
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param service
     * @param deploymentModel 
     * @return the new model instance
	 */
    public static IModel newModel(String groupId, String artifactId, String version, String service, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, List<TransportEnum> transports) {
		return doCreateNewModel(groupId, artifactId, version, service, muleVersion, deploymentModel, transports, null, null, TransformerEnum.JAVA, null, null);
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
    public static IModel newModel(String groupId, String artifactId, String version, String service, MuleVersionEnum muleVersion, List<TransportEnum> transports, String serviceDescriptor, List<String> operations) {
		return doCreateNewModel(groupId, artifactId, version, service, muleVersion, null, transports, null, null, TransformerEnum.JAVA, serviceDescriptor, operations);
    }

	/**
	 * Constructor-method to use when service descriptors are required (e.g. schema and wsdl for services)
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param service
	 * @param deploymentModel 
	 * @param serviceDescriptor
	 * @param operations
     * @return the new model instance
     */
    private static IModel doCreateNewModel(String groupId, String artifactId, String version, String service, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, List<TransportEnum> transports, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String serviceDescriptor, List<String> operations) {
		try {
	    	DefaultModelImpl m = (DefaultModelImpl)modelClass.newInstance();
	    	m.initModel(groupId, artifactId, version, service, muleVersion, deploymentModel, transports, inboundTransport, outboundTransport, transformerType, serviceDescriptor, operations);
	    	return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

}
