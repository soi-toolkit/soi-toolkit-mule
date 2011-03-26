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
package org.soitoolkit.commons.mule.ftp;

import static org.soitoolkit.commons.mule.util.MuleUtil.getImmutableEndpoint;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.mule.api.MuleContext;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.transport.ftp.FtpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for the FTP-transport.
 * 
 * @author Magnus Larsson
 */
public class FtpUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);
	
	public static final int FTP_FILE_NOT_FOUND = 550;
	public static final int FTP_PATH_CREATED   = 257;

	/**
	 * Ensures that the directory exists and is writable by deleting the
	 * directory and then recreate it.
	 * 
	 * @param muleContext 
	 * @param endpointName
	 * @throws Exception 
	 */
	public static void initEndpointDirectory(MuleContext muleContext, String endpointName) throws Exception {

		logger.info("Init directory for endpoint: {}", endpointName);
		
		FTPClient ftpClient = null;

		try {
			ftpClient = getFtpClient(muleContext, endpointName);
	
			EndpointURI endpointURI = getImmutableEndpoint(muleContext, endpointName).getEndpointURI();
			String path = endpointURI.getPath();
	
			try {
				if (path.startsWith("/~/")) {
					path = path.substring(3); // Strip off the leading "/~/"
				}
				recursiveDeleteDirectory(ftpClient, path);
				recursiveCreateDirectory(ftpClient, path);
			} catch (IOException e) {
				if (logger.isErrorEnabled()) logger.error("Failed to recursivly delete endpoint " + endpointName, e);
			}
			
		} finally {
			if (ftpClient != null) {
				ftpClient.disconnect();
			}
		}
	}
	
	/**
	 * Deletes a directory with all its files and sub-directories.
	 * 
	 * @param ftpClient
	 * @param path
	 * @throws IOException
	 */
	static public void recursiveDeleteDirectory(FTPClient ftpClient, String path) throws IOException {

		logger.info("Delete directory: {}", path);
		
		FTPFile[] ftpFiles = ftpClient.listFiles(path);
		logger.debug("Number of files that will be deleted: {}", ftpFiles.length);
		
		for (FTPFile ftpFile : ftpFiles){
			String filename = path + "/" + ftpFile.getName();
			if (ftpFile.getType() == FTPFile.FILE_TYPE) {
				boolean deleted = ftpClient.deleteFile(filename);
				logger.debug("Deleted {}? {}", filename, deleted);
			} else {
				recursiveDeleteDirectory(ftpClient, filename);
			}
		}

		boolean dirDeleted = ftpClient.deleteFile(path);
		logger.debug("Directory {} deleted: {}", path, dirDeleted);
	}

	/**
	 * Create a directory and all missing parent-directories.
	 * 
	 * @param ftpClient
	 * @param path
	 * @throws IOException
	 */
	static public void recursiveCreateDirectory(FTPClient ftpClient, String path) throws IOException {

		logger.info("Create Directory: {}", path);
		int createDirectoryStatus = ftpClient.mkd(path); // makeDirectory...
		logger.debug("Create Directory Status: {}", createDirectoryStatus);
		
		if (createDirectoryStatus == FTP_FILE_NOT_FOUND) {
			int sepIdx = path.lastIndexOf('/');
			if (sepIdx > -1) {
				String parentPath = path.substring(0, sepIdx);
				recursiveCreateDirectory(ftpClient, parentPath);

				logger.debug("2'nd CreateD irectory: {}", path);
				createDirectoryStatus = ftpClient.mkd(path); // makeDirectory...
				logger.debug("2'nd Create Directory Status: {}", createDirectoryStatus);
			}
		}
	}
	
	private static FTPClient getFtpClient(MuleContext muleContext, String endpointName) throws Exception {
		
		ImmutableEndpoint endpoint = getImmutableEndpoint(muleContext, endpointName);
		FtpConnector c = (FtpConnector)endpoint.getConnector();
		
		return c.getFtp(endpoint.getEndpointURI());
	}	
	
}
