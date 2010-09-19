package org.soitoolkit.commons.mule.util;

/**
 * Simple container that can be used as a final variable but hold volatile objects
 * 
 * @author Magnus Larsson
 *
 * @param <T>
 */
public class ValueHolder<T> {
	public T value = null;
}
