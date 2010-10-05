package org.soitoolkit.tools.generator.plugin.model.enums;

public enum MuleVersionEnum implements ILabeledEnum { 
	MULE_2_2_1("2.2.1"), MULE_2_2_5("2.2.5"), MULE_3_0_0("3.0.0"); 
	
	public static MuleVersionEnum get(int ordinal) {
		return values()[ordinal];
	}

	private String label;
	private MuleVersionEnum(String label) {
		this.label = label;
	}

	// For display in the wizard
	public String getLabel() {return "v" + label;}

	// For generators to point out the right pom-file...
	public String getPomSuffix() {return label;}

}

