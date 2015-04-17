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

import java.util.ArrayList;
import java.util.List;

public enum MuleVersionEnum implements ILabeledEnum {
    MULE_3_3_1_DEPRECATED (3,3,1, "current"),
    MULE_3_4_0            (3,4,0, "current"),
    MULE_3_4_0_EE         (3,4,0, "current"),
	MULE_3_5_0            (3,5,0, "current"),
	MULE_3_6_1            (3,6,1, "current");

	public static final MuleVersionEnum MAIN_MULE_VERSION = MULE_3_5_0;
	
	public static MuleVersionEnum get(int ordinal) {
		return values()[ordinal];
	}

    public static MuleVersionEnum getNonDeprecated(int index) {
        return MuleVersionEnum.getNonDeprecatedVersions().get(index);
    }
    
    public static List<MuleVersionEnum> getNonDeprecatedVersions() {
        List<MuleVersionEnum> versions = new ArrayList<MuleVersionEnum>();
        for (MuleVersionEnum v : values()) {
            if (!v.isDeprecatedVersion()) {
                versions.add(v);
            }
        }
        return versions;
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
	
	private int major;
	private int minor;
	private int revision;
	private String label;
	private String xsdNsVersion;
	
	private MuleVersionEnum(int major, int minor, int revision, String xsdNsVersion) {
		this.major    = major;
		this.minor    = minor;
		this.revision = revision;
		this.label    = major + "." + minor + "." + revision;
		this.xsdNsVersion = xsdNsVersion;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getRevision() {
		return revision;
	}

	/**
	 * Compares two mule versions with each other
	 * 
	 * @param other the other mule version
	 * @return 1 if this version is newer (higher), 0 if they are the same, -1 if the other version is newer (higher)
	 */
	public int compare(String otherMuleVersion) {
		return compare(MuleVersionEnum.getByLabel(otherMuleVersion));
	}

	/**
	 * Compares two mule versions with each other
	 * 
	 * @param other the other mule version
	 * @return 1 if this version is newer (higher), 0 if they are the same, -1 if the other version is newer (higher)
	 */
	public int compare(MuleVersionEnum other) {

		// return 0 if same version
		if (this.label.equals(other.label)) {
			return 0;
		}

		// return 1 if this version is newer (higher)
		if ((major > other.getMajor()) ||
		    (major == other.getMajor() && minor > other.getMinor()) ||
		    (major == other.getMajor() && minor == other.getMinor() && revision > other.getRevision())) {
		
			return 1;

		} else {
			return -1;
		}
			
	}
	
	// For display in the wizard
	public String getLabel() {return isEEVersion() ? "v" + label + "-EE": "v" + label;}

	// For generators to point out the right pom-file...
	public String getPomSuffix() {return label;}

	// For generators to point out the right version number to use in the xsd namespaces
	public String getXsdNsVersion() {return xsdNsVersion;}

    public boolean isEEVersion() {
        return toString().endsWith("_EE");
    }

    public boolean isDeprecatedVersion() {
        return toString().endsWith("_DEPRECATED");
    }

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