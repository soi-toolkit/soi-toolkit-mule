package org.soitoolkit.commons.mule.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.transport.file.FileConnector;

/**
 * Extracts the first file in a zip-file
 * Expects the zip-file as byte-array or an inputStream and returns the content of the fisrt file as a byte-array
 * 
 * @author magnus larsson
 *
 */
public class UnzipTransformer extends AbstractMessageAwareTransformer {

	public UnzipTransformer() {
	    registerSourceType(byte[].class);
	    setReturnClass(byte[].class);
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
		ByteArrayOutputStream dest = null;
		byte[] result = null;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				
				// Skip folders...
				if (entry.isDirectory()) continue;

				String name = entry.getName();
				int lastDirSep = name.lastIndexOf('/');
				if (lastDirSep != -1) {
					logger.debug("unzip strips zip-folderpath " + name.substring(0, lastDirSep));
					name = name.substring(lastDirSep + 1);
				}
				if (logger.isDebugEnabled()) {
					String oldname = message.getProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME).toString();
					logger.debug("unzip replaces original filename " + oldname + " with " + name);
				}
				message.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, name);

				
				dest = new ByteArrayOutputStream();
				IOUtils.copy(zis, dest);

				dest.flush();
				dest.close();

				result = dest.toByteArray();
				if (logger.isDebugEnabled()) {
					logger.debug("unzip extracted " + result.length + " bytes");
				}
				
				// Bail out of this while-loop after the first file...
				break;
			}
			zis.close();
		} catch (IOException ioException) {
			throw new TransformerException(MessageFactory.createStaticMessage("Failed to uncompress file."), this, ioException);
		} finally {
			IOUtils.closeQuietly(dest);
			IOUtils.closeQuietly(zis);
		}
		return result;
	}
}