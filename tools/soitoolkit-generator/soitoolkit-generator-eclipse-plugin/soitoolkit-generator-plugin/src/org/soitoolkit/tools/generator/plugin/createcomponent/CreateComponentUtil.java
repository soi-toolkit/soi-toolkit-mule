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
package org.soitoolkit.tools.generator.plugin.createcomponent;

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.ModelFactory;
import org.soitoolkit.tools.generator.model.enums.ComponentEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;

public class CreateComponentUtil {

//	public static final int INTEGRATION_COMPONENT = 0;
//	public static final int UTILITY_COMPONENT = 1;
//	public static final int SD_SCHEMA_COMPONENT = 2;
//	public static final int IM_SCHEMA_COMPONENT = 3;

	public static String getComponentProjectName(int componentType, String groupId, String artifactId) {
		IModel m = ModelFactory.newModel(groupId, artifactId, null, null, MuleVersionEnum.MAIN_MULE_VERSION, null, null);
		String projectFolderName = null;
		ComponentEnum compEnum = ComponentEnum.get(componentType);
		switch (compEnum) {
		case INTEGRATION_COMPONENT:
			projectFolderName = m.getIntegrationComponentProject();
			break;
		case INTEGRATION_TESTSTUBS_COMPONENT:
			projectFolderName = m.getTeststubStandaloneProject();
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
