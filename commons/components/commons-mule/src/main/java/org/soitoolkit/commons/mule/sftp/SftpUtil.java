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
package org.soitoolkit.commons.mule.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.service.Service;
import org.mule.module.client.MuleClient;
import org.mule.transport.sftp.SftpClient;
import org.mule.transport.sftp.SftpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.MiscUtil;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

/**
 * Helper methods for the SFTP-transport.
 * TODO: These methods should be moved to the SFT-transport SftpUtil-class!
 * 
 * @author Magnus Larsson
 *
 */
public class SftpUtil {

	private static final Logger logger = LoggerFactory.getLogger(SftpUtil.class);

	/**
	 * Hidden constructor.
	 */
	private SftpUtil() {
		throw new UnsupportedOperationException(
				"Not allowed to create an instance of this class");
	}

	/**
	 * Initiates a list of sftp-endpoint-directories. Ensures that affected
	 * services are stopped during the initiation.
	 * 
	 * @param serviceNames
	 * @param endpointNames
	 * @param muleContext
	 * @throws Exception
	 */
	public static void initEndpointDirectories(MuleContext muleContext, String[] serviceNames, String[] endpointNames) throws Exception {

		// Stop all named services
		List<Service> services = new ArrayList<Service>();
		for (String serviceName : serviceNames) {
			try {
				Service service = muleContext.getRegistry().lookupService(
						serviceName);
				service.stop();
				services.add(service);
			} catch (Exception e) {
				logger.error("Error '" + e.getMessage()
						+ "' occured while stopping the service " + serviceName
						+ ". Perhaps the service did not exist in the config?");
				throw e;
			}
		}

		// Now init the directory for each named endpoint, one by one
		for (String endpointName : endpointNames) {
			initEndpointDirectory(endpointName);
		}

		// We are done, startup the services again so that the test can begin...
		for (Service service : services) {
			service.start();
		}
	}

	/**
	 * Ensures that the directory exists and is writable by deleting the
	 * directory and then recreate it.
	 * 
	 * @param endpointName
	 * @throws org.mule.api.MuleException
	 * @throws java.io.IOException
	 * @throws com.jcraft.jsch.SftpException
	 */
	public static void initEndpointDirectory(String endpointName) throws MuleException, IOException, SftpException {
		MuleClient muleClient = new MuleClient();
		SftpClient sftpClient = getSftpClient(muleClient, endpointName);
		try {
			ChannelSftp channelSftp = sftpClient.getChannelSftp();
			try {
				recursiveDelete(muleClient, sftpClient, endpointName, "");
			} catch (IOException e) {
				if (logger.isErrorEnabled())
					logger.error("Failed to recursivly delete endpoint " + endpointName, e);
			}

			String path = getPathByEndpoint(muleClient, sftpClient, endpointName);
			mkDirs(channelSftp, path);
		} finally {
			sftpClient.disconnect();
			if (logger.isDebugEnabled())
				logger.debug("Done init endpoint directory: " + endpointName);
		}
	}

	
	public static String[] getFilesInEndpoint(String endpointName, String subDirectory) throws IOException, MuleException {
        MuleClient muleClient = new MuleClient();
    	SftpClient sftpClient = getSftpClient(muleClient, endpointName);
        ImmutableEndpoint tEndpoint = (ImmutableEndpoint) muleClient.getProperty(endpointName);
        try {
      	String path = tEndpoint.getEndpointURI().getPath();
      	if (subDirectory != null) {
      		path += '/' + subDirectory;
      	}
          String[] files = getFilesInPath(sftpClient, path);
          return files;
          
        } finally {
          sftpClient.disconnect();
        }
      }

	public static String[] getFilesInPath(SftpClient sftpClient, String path) throws IOException {
      sftpClient.changeWorkingDirectory(sftpClient.getAbsolutePath(path));
      String[] files = sftpClient.listFiles();

      return files;
    }
    
	public static String getSftpFileContent(String endpointName, String file) throws MuleException, IOException {
        MuleClient muleClient = new MuleClient();
    	SftpClient sftpClient = getSftpClient(muleClient, endpointName);
        ImmutableEndpoint tEndpoint = (ImmutableEndpoint) muleClient.getProperty(endpointName);
        try {
          	String path = tEndpoint.getEndpointURI().getPath();
            sftpClient.changeWorkingDirectory(sftpClient.getAbsolutePath(path));      	

            InputStream is = sftpClient.retrieveFile(file);
            return MiscUtil.convertStreamToString(is);

        } finally {
          sftpClient.disconnect();
        }
	}

	/*
	 * Private parts...
	 */	
	
	/**
	 * TODO: Optimize to only create not existing folders and skip trying to create already existing ones...
	 * 
	 * @param channelSftp
	 * @param path
	 */
	private static void mkDirs(ChannelSftp channelSftp, String path) {
//		System.err.println("### CREATE SFTP-PATH: " + path);
		int lastSepPos = path.lastIndexOf('/');
		if (lastSepPos > 0) {
			String parentPath = path.substring(0, lastSepPos);
			mkDirs(channelSftp, parentPath);
		}
		try {
			channelSftp.mkdir(path);
		} catch(SftpException e) {
//			System.err.println("### FAILD TO CREATE PATH FOR: " + path + ", ERROR: " + e.getMessage());			
		}
	}

	/**
	 * Returns a SftpClient that is logged in to the sftp server that the
	 * endpoint is configured against.
	 * 
	 * @param muleClient
	 * @param endpointName
	 * @return
	 * @throws IOException
	 */
	static protected SftpClient getSftpClient(MuleClient muleClient,
			String endpointName) throws IOException {
		ImmutableEndpoint endpoint = getImmutableEndpoint(muleClient,
				endpointName);
		EndpointURI endpointURI = endpoint.getEndpointURI();
		SftpClient sftpClient = new SftpClient(endpointURI.getHost());

		SftpConnector sftpConnector = (SftpConnector) endpoint.getConnector();

		if (sftpConnector.getIdentityFile() != null) {
			try {
				sftpClient.login(endpointURI.getUser(),
						sftpConnector.getIdentityFile(),
						sftpConnector.getPassphrase());
			} catch (Exception e) {
				throw new RuntimeException("Login failed", e);
			}
		} else {
			try {
				sftpClient.login(endpointURI.getUser(),
						endpointURI.getPassword());
			} catch (Exception e) {
				throw new RuntimeException("Login failed", e);
			}
		}
		return sftpClient;
	}

	static protected String getPathByEndpoint(MuleClient muleClient,
			SftpClient sftpClient, String endpointName) {
		ImmutableEndpoint endpoint = (ImmutableEndpoint) muleClient
				.getProperty(endpointName);
		EndpointURI endpointURI = endpoint.getEndpointURI();

		return sftpClient.getAbsolutePath(endpointURI.getPath());
	}

	static protected ImmutableEndpoint getImmutableEndpoint(
			MuleClient muleClient, String endpointName) throws IOException {
		ImmutableEndpoint endpoint = null;

		Object o = muleClient.getProperty(endpointName);
		if (o instanceof ImmutableEndpoint) {
			// For Inbound and Outbound Endpoints
			endpoint = (ImmutableEndpoint) o;

		} else if (o instanceof EndpointBuilder) {
			// For Endpoint-references
			EndpointBuilder eb = (EndpointBuilder) o;
			try {
				endpoint = eb.buildInboundEndpoint();
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}
		return endpoint;
	}

	static protected EndpointURI getUriByEndpointName(MuleClient muleClient,
			String endpointName) throws IOException {
		ImmutableEndpoint endpoint = getImmutableEndpoint(muleClient,
				endpointName);
		return endpoint.getEndpointURI();
	}

	/**
	 * Deletes a directory with all its files and sub-directories. The reason it
	 * do a "chmod 700" before the delete is that some tests changes the
	 * permission, and thus we have to restore the right to delete it...
	 * 
	 * @param muleClient
	 * @param endpointName
	 * @param relativePath
	 * @throws IOException
	 */
	static protected void recursiveDelete(MuleClient muleClient,
			SftpClient sftpClient, String endpointName, String relativePath)
			throws IOException {
		EndpointURI endpointURI = getUriByEndpointName(muleClient, endpointName);
		String path = endpointURI.getPath() + relativePath;

		try {
			// Ensure that we can delete the current directory and the below
			// directories (if write is not permitted then delete is either)
			sftpClient.chmod(path, 00700);

			sftpClient.changeWorkingDirectory(sftpClient.getAbsolutePath(path));

			// Delete all sub-directories
			String[] directories = sftpClient.listDirectories();
			for (String directory : directories) {
				recursiveDelete(muleClient, sftpClient, endpointName,
						relativePath + "/" + directory);
			}

			// Needs to change the directory back after the recursiveDelete
			sftpClient.changeWorkingDirectory(sftpClient.getAbsolutePath(path));

			// Delete all files
			String[] files = sftpClient.listFiles();
			for (String file : files) {
				sftpClient.deleteFile(file);
			}

			// Delete the directory
			try {
				sftpClient.deleteDirectory(path);
			} catch (Exception e) {
				if (logger.isDebugEnabled())
					logger.debug("Failed delete directory " + path, e);
			}

		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("Failed to recursivly delete directory " + path, e);
		}
	}
}
