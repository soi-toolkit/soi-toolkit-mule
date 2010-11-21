package org.soitoolkit.commons.mule.zip;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transport.file.FileConnector;
import org.soitoolkit.commons.mule.util.MiscUtil;

public class UnzipTransformerTest {

	@Test
	public void testUnzipOneFileNoFolders() throws FileNotFoundException, TransformerException {
		performUnzipTest("src/test/resources/testfiles/zip/one-file-no-folders.zip");
	}

	@Test
	public void testUnzipOneFileOneFolder() throws FileNotFoundException, TransformerException {
		performUnzipTest("src/test/resources/testfiles/zip/one-file-one-folder.zip");
	}

	private void performUnzipTest(String zipFile) throws FileNotFoundException, TransformerException {
		FileInputStream is = new FileInputStream(zipFile);
		MuleMessage msg = new DefaultMuleMessage(is);
		UnzipTransformer transformer = new UnzipTransformer();
		String expectedResult = MiscUtil.readFileAsString("src/test/resources/testfiles/zip/i2-input.txt") + '\n';

		// Perform the unzip
		Object result = transformer.transform(msg);

		// Verify that we got an byte-array back
		assertTrue(result instanceof byte[]);
		
		// Verify its content
		String resultString = new String((byte[])result);
		assertEquals(expectedResult, resultString);

		// Verify that the filename was picked up correctly
		String filename = msg.getProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME).toString();
		assertEquals("i2-input.txt", filename);
	}

}
