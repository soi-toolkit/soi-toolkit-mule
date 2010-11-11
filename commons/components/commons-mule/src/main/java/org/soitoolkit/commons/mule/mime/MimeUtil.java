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
package org.soitoolkit.commons.mule.mime;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various helper methods use for mime related processing
 * 
 * @author Magnus Larsson
 *
 */
public class MimeUtil {

	private final static Logger logger = LoggerFactory.getLogger(MimeUtil.class);

	/**
     * Hidden constructor.
     */
    private MimeUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

	public static String sendFileAsMultipartHttpPost(String targetURL, File targetFile, boolean expectHeader, int timeoutMs) {
		
		logger.debug("Send file {} to url {}", targetFile.getAbsolutePath(), targetURL);

		String response = null;
		
		PostMethod filePost = new PostMethod(targetURL);
        
		filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, expectHeader);

        try {
            
            Part[] parts = {
                new FilePart(targetFile.getName(), targetFile)
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(timeoutMs);
            
            int status = client.executeMethod(filePost);

            logger.debug("Send done, http status: {}", status);

            if (status == HttpStatus.SC_OK) {
                response = filePost.getResponseBodyAsString();
                logger.debug("Send done, http response: {}", response);
            } else {
                String errorText = HttpStatus.getStatusText(status);
                throw new RuntimeException("HTTP Error Code: " + status + "HTTP Error Text: " + errorText);
            }
        
        } catch (IOException e) {
        	throw new RuntimeException(e);
        } finally {
            filePost.releaseConnection();
        }

        return response;
	}
 
}
