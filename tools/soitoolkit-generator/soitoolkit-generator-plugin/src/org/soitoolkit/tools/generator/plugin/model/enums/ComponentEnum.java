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
package org.soitoolkit.tools.generator.plugin.model.enums;

public enum ComponentEnum implements ILabeledEnum { 
	INTEGRATION_COMPONENT("Integration Component"), UTILITY_COMPONENT("Utility Component"), SD_SCHEMA_COMPONENT("Service Description Component"); //, IM_SCHEMA_COMPONENT("Information Model Component"); 

	public static ComponentEnum get(int ordinal) {
		return values()[ordinal];
	}

	private String label;
	private ComponentEnum(String label) {
		this.label = label;
	}
	public String getLabel() {return label;}
}

