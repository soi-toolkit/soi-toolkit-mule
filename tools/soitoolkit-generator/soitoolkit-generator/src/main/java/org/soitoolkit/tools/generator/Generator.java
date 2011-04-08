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
package org.soitoolkit.tools.generator;

public interface Generator {
	
	public static final String GEN_METADATA_ARTIFACT_ID_KEY = "soi-toolkit.gen.artifactId";
	public static final String GEN_METADATA_TYPE_KEY        = "soi-toolkit.gen.type";

	public static final String GEN_METADATA_TYPE_INTEGRATION_COMPONENT         = "integrationComponent";
	public static final String GEN_METADATA_TYPE_STANDALONE                    = "standalone";
	public static final String GEN_METADATA_TYPE_TESTSTUB_STANDALONE           = "teststubStandalone";
	public static final String GEN_METADATA_TYPE_WAR                           = "war";
	public static final String GEN_METADATA_TYPE_TESTSTUB_WAR                  = "teststubWar";
	public static final String GEN_METADATA_TYPE_SERVICE                       = "services";

	public static final String GEN_METADATA_TYPE_SERVICE_DESCRIPTION_COMPONENT = "schemas";
	
    public void startGenerator();

}
