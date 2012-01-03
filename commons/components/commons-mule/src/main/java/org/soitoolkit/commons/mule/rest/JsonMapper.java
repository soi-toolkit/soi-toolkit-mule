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
package org.soitoolkit.commons.mule.rest;

import java.io.InputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonMapper {

	private ObjectMapper mapper = new ObjectMapper();
	public String marshal(Object jsonObject) {
    	try {
			return mapper.writeValueAsString(jsonObject);
		} catch (Exception e) {
			throw new  RuntimeException(e);
		}
	}
	public <T> T unmarshal(String jsonString, Class<T> valueType) {
		try {
			JsonParser jp = new JsonFactory().createJsonParser(jsonString);
	    	return unmarshal(jp, valueType);
		} catch (Exception e) {
			throw new  RuntimeException(e);
		}
	}
	public <T> T unmarshal(InputStream jsonStream, Class<T> valueType) {
		try {
			JsonParser jp = new JsonFactory().createJsonParser(jsonStream);
	    	return unmarshal(jp, valueType);
		} catch (Exception e) {
			throw new  RuntimeException(e);
		}
	}
	private <T> T unmarshal(JsonParser jpExpected, Class<T> valueType) throws Exception {
    	return mapper.readValue(jpExpected, valueType);
	}
	
}
