package org.soitoolkit.tools.generator.plugin.model.enums;

public class EnumUtil {

    /**
	 * Hidden constructor.
	 */
	private EnumUtil() {
		throw new UnsupportedOperationException("Not allowed to create an instance of this class");
	}

	static public String[] getLabels(ILabeledEnum[] meps) {
		String[] labels = new String[meps.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = meps[i].getLabel();
		}
		return labels;
	}

	
}
