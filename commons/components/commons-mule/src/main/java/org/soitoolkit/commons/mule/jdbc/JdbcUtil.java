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

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.MuleUtil;


/**
 * Various helper methods for JDBC
 * 
 * @author Magnus Larsson
 *
 */
public class JdbcUtil {

	private final static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

	/**
     * Hidden constructor.
     */
    private JdbcUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }
    
    public DataSource lookupDataSource(String dataSourceName) {
    	log.debug("Lookup datasource named {}", dataSourceName);
	    return (DataSource)MuleUtil.getSpringBean(dataSourceName);
    }

}
