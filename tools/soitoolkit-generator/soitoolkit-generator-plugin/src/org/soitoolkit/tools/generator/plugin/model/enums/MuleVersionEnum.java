package org.soitoolkit.tools.generator.plugin.model.enums;

public enum MuleVersionEnum implements ILabeledEnum { 
	MULE_2_2_1("v2.2.1"), MULE_2_2_5("v2.2.5"), MULE_3_0_0("v3.0.0"); 
	
	public static MuleVersionEnum get(int ordinal) {
		return values()[ordinal];
	}

	private String label;
	private MuleVersionEnum(String label) {
		this.label = label;
	}
	public String getLabel() {return label;}
}

