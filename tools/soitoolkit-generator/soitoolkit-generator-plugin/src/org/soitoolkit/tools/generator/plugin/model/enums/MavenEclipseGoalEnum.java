package org.soitoolkit.tools.generator.plugin.model.enums;

public enum MavenEclipseGoalEnum implements ILabeledEnum { 
	ECLIPSE_ECLIPSE("eclipse:eclipse"), ECLIPSE_M2ECLIPSE("eclipse:m2eclipse"); 
	
	public static MavenEclipseGoalEnum get(int ordinal) {
		return values()[ordinal];
	}

	private String label;
	private MavenEclipseGoalEnum(String label) {
		this.label = label;
	}
	public String getLabel() {return label;}
}

