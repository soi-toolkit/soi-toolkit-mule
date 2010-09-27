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

