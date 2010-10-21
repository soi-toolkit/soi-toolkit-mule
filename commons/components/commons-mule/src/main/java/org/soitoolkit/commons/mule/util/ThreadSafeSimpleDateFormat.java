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
package org.soitoolkit.commons.mule.util;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread safe version of the SimplDateFormat - class.
 * 
 * Based on code published at: http://www.javaspecialists.eu/archive/Issue172.html
 * 
 * @author Magnus Larsson
 *
 */
public class ThreadSafeSimpleDateFormat {

	  private final ThreadLocal<SoftReference<DateFormat>> tl
	      = new ThreadLocal<SoftReference<DateFormat>>();

	  private DateFormat getDateFormat() {
	    SoftReference<DateFormat> ref = tl.get();
	    if (ref != null) {
	      DateFormat result = ref.get();
	      if (result != null) {
	        return result;
	      }
	    }
	    DateFormat result = new SimpleDateFormat(pattern);
	    ref = new SoftReference<DateFormat>(result);
	    tl.set(ref);
	    return result;
	  }

	  private String pattern = null;
	  
	  public ThreadSafeSimpleDateFormat(String pattern) {
		  this.pattern = pattern;
	  }
	  
	  public String format(Date date) {
	      return getDateFormat().format(date);
	  }

	  public Date parse(String date) throws ParseException {
		      return getDateFormat().parse(date);
	  }
	}