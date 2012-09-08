package org.soitoolkit.tools.generator.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.soitoolkit.tools.generator.util.SourceFormatterUtil.EOL_UNIX_STYLE_LF;
import static org.soitoolkit.tools.generator.util.SourceFormatterUtil.EOL_WINDOWS_STYLE_CRLF;

public class SourceFormatterUtilTest {
	String srcWithDifferentEOLs;

	@Before
	public void setUp() {
		StringBuilder sbSrcWithDifferentEOLs = new StringBuilder();
		sbSrcWithDifferentEOLs
				.append("first line ending with CR+LF (Windows style) line endings\r\n");
		sbSrcWithDifferentEOLs
				.append("second line ending with LF (Unix style) line endings\n");
		sbSrcWithDifferentEOLs.append("third line LF\n");
		sbSrcWithDifferentEOLs.append("fourth line CR+LF\r\n");

		srcWithDifferentEOLs = sbSrcWithDifferentEOLs.toString();
	}

	@Test
	public void testAssertCurrentLineSeparatorIsAllowed() {
		SourceFormatterUtil
				.staticSetCurrentLineSeparatorForTesting(EOL_WINDOWS_STYLE_CRLF);
		SourceFormatterUtil.assertCurrentLineSeparatorIsAllowed();
		SourceFormatterUtil
				.staticSetCurrentLineSeparatorForTesting(EOL_UNIX_STYLE_LF);
		SourceFormatterUtil.assertCurrentLineSeparatorIsAllowed();
		try {
			SourceFormatterUtil
					.staticSetCurrentLineSeparatorForTesting("UNKNOWN_EOL");
			SourceFormatterUtil.assertCurrentLineSeparatorIsAllowed();
			Assert.fail("expected exception");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("unknown"));
		}
	}

	@Test
	public void testFormatSource_CRLF_Windows_platform() {
		SourceFormatterUtil
				.staticSetCurrentLineSeparatorForTesting(EOL_WINDOWS_STYLE_CRLF);
		String result = SourceFormatterUtil.formatSource(srcWithDifferentEOLs);
		// make sure every line ends with CR + LF
		String lines[] = result.split("\n");
		Assert.assertEquals(4, lines.length);
		for (String line : lines) {
			Assert.assertTrue(line.endsWith("\r"));
		}
	}

	@Test
	public void testFormatSource_LF_Unix_platform() {
		SourceFormatterUtil
				.staticSetCurrentLineSeparatorForTesting(EOL_UNIX_STYLE_LF);
		String result = SourceFormatterUtil.formatSource(srcWithDifferentEOLs);
		Assert.assertFalse(result.contains(EOL_WINDOWS_STYLE_CRLF));
	}

}
