package org.soitoolkit.tools.generator.plugin.model.enums;

public enum MepEnum implements ILabeledEnum { 
	MEP_REQUEST_RESPONSE("Request/Response"), MEP_ONE_WAY("One Way"), MEP_PUBLISH_SUBSCRIBE("Publish/Subscribe"); 
	
	public static MepEnum get(int ordinal) {
		return values()[ordinal];
	}

	private String label;
	private MepEnum(String label) {
		this.label = label;
	}
	public String getLabel() {return label;}
}

