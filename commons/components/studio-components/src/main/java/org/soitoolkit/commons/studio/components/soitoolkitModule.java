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
package org.soitoolkit.commons.studio.components;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;

/**
 * Generic module
 *
 * @author MuleSoft, Inc.
 */
@Module(name="soitoolkit", schemaVersion="0.5.1-SNAPSHOT")
public class soitoolkitModule
{
    /**
     * Configurable
     */
    @Configurable
    private String myProperty;

    /**
     * Set property
     *
     * @param myProperty My property
     */
    public void setMyProperty(String myProperty)
    {
        this.myProperty = myProperty;
    }

    /**
     * Get property
     */
    public String getMyProperty()
    {
        return this.myProperty;
    }

    /**
     * Custom processor
     *
     * {@sample.xml ../../../doc/soitoolkit-connector.xml.sample soitoolkit:my-processor}
     *
     * @param content Content to be processed
     * @return Some string
     */
    @Processor
    public String myProcessor(String content)
    {
        /*
         * MESSAGE PROCESSOR CODE GOES HERE
         */
    	System.err.println("### MY COMP WORKS!");
        return content;
    }
}
