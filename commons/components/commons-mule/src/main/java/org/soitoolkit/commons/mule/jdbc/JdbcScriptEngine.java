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
package org.soitoolkit.commons.mule.jdbc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.MiscUtil;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Executes sql commands from a file
 * 
 * @author Magnus Larsson
 */
public class JdbcScriptEngine {

	private final static Logger log = LoggerFactory.getLogger(JdbcUtil.class);
	
	private SimpleJdbcTemplate t;
	
	public JdbcScriptEngine(DataSource ds) {
		t = new SimpleJdbcTemplate(ds);
	}
	
	public void execute(String scriptFilename) throws FileNotFoundException {
		String s = MiscUtil.convertStreamToString(new FileInputStream(scriptFilename));
		log.info("Execute JDBC-scriptfile: {}", scriptFilename);
		t.getJdbcOperations().execute(s);
		log.info("Done executing JDBC-scriptfile: {}", scriptFilename);
	}
}
