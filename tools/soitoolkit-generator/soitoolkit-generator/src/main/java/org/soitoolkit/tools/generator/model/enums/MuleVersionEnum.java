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
package org.soitoolkit.tools.generator.model.enums;

public enum MuleVersionEnum implements ILabeledEnum { 
	MULE_3_2_0("3.2.0", "current"),
	MULE_3_2_1("3.2.1", "current");

	public static final MuleVersionEnum MAIN_MULE_VERSION = MULE_3_2_1;
	
	public static MuleVersionEnum get(int ordinal) {
		return values()[ordinal];
	}

	public static MuleVersionEnum getByLabel(String label) {
	    for(MuleVersionEnum e : values()) {
	        if(e.getPomSuffix().equals(label)){
	            return e;
	        }
	    }
	    return null;
	}
	
	public static String allowedLabelValues() {
		String allowedLabelValues = "";
		for (MuleVersionEnum muleVersion : values()) {
			allowedLabelValues += muleVersion.getPomSuffix() + " ";
		}
	    return allowedLabelValues;
	}
	
	private String label;
	private String xsdNsVersion;
	private MuleVersionEnum(String label, String xsdNsVersion) {
		this.label = label;
		this.xsdNsVersion = xsdNsVersion;
	}

	// For display in the wizard
	public String getLabel() {return "v" + label;}

	// For generators to point out the right pom-file...
	public String getPomSuffix() {return label;}

	// For generators to point out the right version number to use in the xsd namespaces
	public String getXsdNsVersion() {return xsdNsVersion;}

	// For ver no with only numbers...
	public String getVerNoNumbersOnly() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < label.length(); i++) {
			char c = label.charAt(i);
			if (Character.isDigit(c)) sb.append(c);
		}
		return sb.toString();
	}

}

