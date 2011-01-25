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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.MessageFactory;
import org.mule.routing.filters.WildcardFilter;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.transport.file.FileConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the first file in a zip-file
 * Expects the zip-file as byte-array or an inputStream and returns the content of the first file matching the filter as an input stream
 * 
 * @author magnus larsson
 *
 */
public class UnzipTransformer extends AbstractMessageAwareTransformer {

	private static final Logger log = LoggerFactory.getLogger(UnzipTransformer.class);

	private WildcardFilter filter = new WildcardFilter("*");

	public UnzipTransformer() {
	    registerSourceType(InputStream.class);
	    registerSourceType(byte[].class);
	    setReturnClass(InputStream.class);	    
	}

	public void setFilenamePattern(String pattern) {
		filter.setPattern(pattern);
	}

	public String getFilenamePattern() {
		return filter.getPattern();
	}
	
	@Override
	public Object transform(MuleMessage message, String encoding) throws TransformerException {
		Object payload = message.getPayload();

		InputStream is = null;
		if (payload instanceof InputStream) {
			is = (InputStream)payload;

		} else if (payload instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) payload);

		} else {
			throw new RuntimeException("Unknown payload type: " + payload.getClass().getName());
		}

		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry = null;
		InputStream result = null;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				
				String name = entry.getName();

				// Skip folders...
				if (entry.isDirectory()) {
					log.debug("skip folder " + name);
					continue;
				}

				// Does the file pass the filename-filter?
				if (!filter.accept(name)) {
					log.debug("skip file " + name + " did not match filename pattern: " + filter.getPattern());
					continue;
				}

				int lastDirSep = name.lastIndexOf('/');
				if (lastDirSep != -1) {
					log.debug("unzip strips zip-folderpath " + name.substring(0, lastDirSep));
					name = name.substring(lastDirSep + 1);
				}
				if (log.isDebugEnabled()) {
					String oldname = message.getProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME).toString();
					log.debug("unzip replaces original filename " + oldname + " with " + name);
				}
				message.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, name);

				result = new BufferedInputStream(zis);
				
				// Bail out of this while-loop after the first file matching the filter...
				break;
			}
		} catch (IOException ioException) {
			throw new TransformerException(MessageFactory.createStaticMessage("Failed to uncompress file."), this, ioException);
		}
		return result;
	}
}