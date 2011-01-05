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
package org.soitoolkit.commons.mule.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.lang.NotImplementedException;

/**
 * Text based DataSource that use UTF-8 encoding, i.e. supporting non US 7-bit ASCII characters.
 * 
 * @author Magnus Larsson
 *
 */
public class TextDataSource implements DataSource {

    public static final String CHARSET      = "UTF-8";
    public static final String CONTENT_TYPE = "text/plain; charset=" + CHARSET;

    private String name = null;
    private String text = null;
    
	public TextDataSource(String name, String text) {
		this.name = name;
		this.text = text;
	}
	
    public String getName() {
        return name;
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(text.getBytes(CHARSET));
    }

    public OutputStream getOutputStream() {
        throw new NotImplementedException();
    }

}