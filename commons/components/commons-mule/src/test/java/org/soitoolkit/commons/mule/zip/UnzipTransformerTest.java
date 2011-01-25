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
package org.soitoolkit.commons.mule.zip;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;
import org.mule.DefaultMuleContext;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.context.notification.ServerNotificationManager;
import org.mule.transport.file.FileConnector;
import org.soitoolkit.commons.mule.util.MiscUtil;
import org.soitoolkit.commons.mule.util.MuleUtil;

/**
 * Tests the unzip transformer with various usage scenarios
 * 
 * @author magnus larsson
 *
 */
public class UnzipTransformerTest {

	@Test
	public void testUnzipOneFileNoFolders() throws FileNotFoundException, TransformerException {
		performUnzipTest("src/test/resources/testfiles/zip/one-file-no-folders.zip");
	}

	@Test
	public void testUnzipOneFileOneFolder() throws FileNotFoundException, TransformerException {
		performUnzipTest("src/test/resources/testfiles/zip/one-file-one-folder.zip");
	}

	@Test
	public void testUnzipThreeFilesOneFolder() throws FileNotFoundException, TransformerException {
		performUnzipTest("src/test/resources/testfiles/zip/three-files-one-folder.zip", "*i2-input.txt");
	}

		
	private void performUnzipTest(String zipFile) throws FileNotFoundException, TransformerException {
		performUnzipTest(zipFile, null);
	}

	private void performUnzipTest(String zipFile, String filenamePattern) throws FileNotFoundException, TransformerException {
		
		ServerNotificationManager nm = new ServerNotificationManager();
		MuleContext muleContext = new DefaultMuleContext(new DefaultMuleConfiguration(), null, null, null, nm );
		
		FileInputStream is = new FileInputStream(zipFile);
		MuleMessage msg = MuleUtil.createMuleMessage(is, muleContext);
		UnzipTransformer transformer = new UnzipTransformer();
		if (filenamePattern != null) transformer.setFilenamePattern(filenamePattern);
		String expectedResult = MiscUtil.readFileAsString("src/test/resources/testfiles/zip/i2-input.txt");

		// Perform the unzip
		Object result = transformer.transform(msg);

		// Verify that we got an input stream back
		assertTrue(result instanceof InputStream);

		// Verify its content
		String resultString = MiscUtil.convertStreamToString((InputStream)result);
		assertEquals(expectedResult, resultString);
		
		// Verify that the filename was picked up correctly
		String filename = msg.getProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME).toString();
		assertEquals("i2-input.txt", filename);
	}

}
