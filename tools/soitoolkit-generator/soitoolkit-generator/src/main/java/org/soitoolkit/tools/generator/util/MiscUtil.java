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
package org.soitoolkit.tools.generator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.soitoolkit.tools.generator.model.enums.TransportEnum;

public class MiscUtil {
	/**
     * Hidden constructor.
     */
    private MiscUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    public static String convertStreamToString(InputStream is) {
    	return convertStreamToString(is, "UTF-8");
    }
    
    /**
     * To convert an InputStream to a String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     * 
     * @param is
     * @return
     */
     public static String convertStreamToString(InputStream is, String charset) {

    	if (is == null) return null;

    	StringBuilder sb = new StringBuilder();
        String line;

        try {
        	// TODO: Can this be a performance killer if many many lines or is BufferedReader handling that in a good way?
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
        	throw new RuntimeException(e);
		} finally {
			// Ignore exceptions on call to the close method
            try {is.close();} catch (IOException e) {}
        }
        return SourceFormatterUtil.formatSource(sb.toString());
    }

     public static TransportEnum[] appendTransport(TransportEnum[] transports, TransportEnum newTransport) {
		TransportEnum[] newArr = new TransportEnum[transports.length + 1];
		System.arraycopy(transports, 0, newArr, 0, transports.length);
		newArr[newArr.length - 1] = newTransport;
		return newArr;
	}

}
