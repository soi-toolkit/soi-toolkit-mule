package org.soitoolkit.tools.generator.util;

/**
 * Format generated source before it is written to disk to make sure we have
 * consistent line-endings.
 * <p>
 * Will use platform line-endings by default, i.e. CR+LF on Windows and LF on
 * Unix-style platforms.
 * 
 * @author Hakan Dahl
 */
public class SourceFormatterUtil {
	static final String EOL_WINDOWS_STYLE_CRLF = "\r\n";
	static final String EOL_UNIX_STYLE_LF = "\n"; 
	private static String currentLineSeparator = System
			.getProperty("line.separator");

	private SourceFormatterUtil() {
		// static access only
	}

	public static String formatSource(String source) {
		return formatEndOfLine(source);
	}

	static String formatEndOfLine(String source) {
		assertCurrentLineSeparatorIsAllowed();
		if (EOL_WINDOWS_STYLE_CRLF.equals(currentLineSeparator)) {
			// first replace all EOL to only LF to to avoid replacing CRLF to
			// CRCRLF
			String s = source
					.replace(EOL_WINDOWS_STYLE_CRLF, EOL_UNIX_STYLE_LF);
			return s.replaceAll(EOL_UNIX_STYLE_LF, EOL_WINDOWS_STYLE_CRLF);
		} else {
			return source.replace(EOL_WINDOWS_STYLE_CRLF, EOL_UNIX_STYLE_LF);
		}
	}

	static void assertCurrentLineSeparatorIsAllowed() {
		if (!EOL_WINDOWS_STYLE_CRLF.equals(currentLineSeparator)
				&& !EOL_UNIX_STYLE_LF.equals(currentLineSeparator)) {
			throw new IllegalStateException(
					"current line separator is unknown: before_"
							+ currentLineSeparator + "_after");
		}
	}

	static void staticSetCurrentLineSeparatorForTesting(String lineSeparator) {
		currentLineSeparator = lineSeparator;
	}
}
