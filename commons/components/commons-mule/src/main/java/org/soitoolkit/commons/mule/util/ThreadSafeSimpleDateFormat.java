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