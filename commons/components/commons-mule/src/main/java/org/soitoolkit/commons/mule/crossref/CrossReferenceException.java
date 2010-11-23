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
package org.soitoolkit.commons.mule.crossref;

/**
 * Runtime exception for various CrossRef related exceptions
 * 
 * @author magnus larsson
 *
 */
public class CrossReferenceException extends RuntimeException {

	private static final long serialVersionUID = -1654654157824883889L;

	private String key;
	
	public CrossReferenceException(String message, String key) {
		super(message);
		this.key = key;
	}
	
	public CrossReferenceException(String message, Throwable ex, String key) {
		super(message, ex);
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
}
