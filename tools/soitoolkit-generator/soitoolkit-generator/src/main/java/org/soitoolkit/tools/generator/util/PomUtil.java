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

import static org.soitoolkit.tools.generator.util.XmlUtil.createDocument;
import static org.soitoolkit.tools.generator.util.XmlUtil.getDocumentComment;
import static org.soitoolkit.tools.generator.util.XmlUtil.getFirstValue;
import static org.soitoolkit.tools.generator.util.XmlUtil.getXPathResult;
import static org.soitoolkit.tools.generator.util.XmlUtil.lookupParameterValue;
import static org.soitoolkit.tools.generator.Generator.GEN_METADATA_ARTIFACT_ID_KEY;

import java.io.InputStream;

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.ModelFactory;
import org.w3c.dom.Document;

public class PomUtil {
	/**
     * Hidden constructor.
     */
    private PomUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

	public static IModel extractGroupIdAndArtifactIdFromPom(InputStream content) {

		Document doc = createDocument(content);

		String docComment = getDocumentComment(doc);
		String artifactId = lookupParameterValue(GEN_METADATA_ARTIFACT_ID_KEY, docComment);

		String nsPrefix = "ns";
		String nsURI = "http://maven.apache.org/POM/4.0.0";
		String parentGroupId = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:parent/ns:groupId/text()"));
		String groupId       = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:groupId/text()"));
		if (groupId == null) groupId = parentGroupId;
		
		return ModelFactory.newModel(groupId, artifactId, null, null, null, null, null);
	}
   
}
