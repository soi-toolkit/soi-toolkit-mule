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
package org.soitoolkit.commons.mule.smooks;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.MiscUtil;
import org.xml.sax.SAXException;

/**
 * Various helper methods used for smookes
 * 
 * @author Magnus Larsson
 *
 */
public class SmooksUtil {

	private final static Logger logger = LoggerFactory.getLogger(SmooksUtil.class);

	/**
     * Hidden constructor.
     */
    private SmooksUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }
 
    static public String runSmooksTransformer(String source, String smookesConfig) throws IOException, SAXException, SmooksException {

        Smooks smooks = new Smooks(smookesConfig);
        StringWriter writer = new StringWriter();

        try {
        	
            ExecutionContext executionContext = smooks.createExecutionContext();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/smooks-report/report.html"));

            smooks.filterSource(executionContext, new StreamSource(new StringReader(source)), new StreamResult(writer));

            String result = writer.toString();
            
            // Remove any leading new lines
            result = MiscUtil.removeLeadingNewLines(result);
            
            // Remove any trailing new lines
            result = MiscUtil.removeTrailingNewLines(result);
            
            logger.debug("Result from smooks transformation [{}]", result);
            return result;

        } finally {
            smooks.close();
            writer.close();
        }
    }
}
