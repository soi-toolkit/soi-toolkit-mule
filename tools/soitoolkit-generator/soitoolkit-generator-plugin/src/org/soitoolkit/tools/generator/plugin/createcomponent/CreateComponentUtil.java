package org.soitoolkit.tools.generator.plugin.createcomponent;

import org.soitoolkit.tools.generator.plugin.model.IModel;
import org.soitoolkit.tools.generator.plugin.model.ModelFactory;
import org.soitoolkit.tools.generator.plugin.model.enums.ComponentEnum;

public class CreateComponentUtil {

//	public static final int INTEGRATION_COMPONENT = 0;
//	public static final int UTILITY_COMPONENT = 1;
//	public static final int SD_SCHEMA_COMPONENT = 2;
//	public static final int IM_SCHEMA_COMPONENT = 3;

	public static String getComponentProjectName(int componentType, String groupId, String artifactId) {
		IModel m = ModelFactory.newModel(groupId, artifactId, null, null, null, null);
		String projectFolderName = null;
		ComponentEnum compEnum = ComponentEnum.get(componentType);
		switch (compEnum) {
		case INTEGRATION_COMPONENT:
			projectFolderName = m.getIntegrationComponentProject();
			break;
		case SD_SCHEMA_COMPONENT:
			projectFolderName = m.getSchemaProject();
			break;
		}
		return projectFolderName;
	}

	/**
     * Hidden constructor.
     */
    private CreateComponentUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }


}
