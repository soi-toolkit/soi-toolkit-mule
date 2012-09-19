package org.soitoolkit.tools.generator.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.ServiceDescriptorModel;
import org.soitoolkit.tools.generator.model.ServiceDescriptorXmlSchemaModel;

public interface ServiceDescriptorModelUtil {
	public ServiceDescriptorModel createServiceDescriptorModel(IModel model, File wsdlFile, File targetDir);
}
