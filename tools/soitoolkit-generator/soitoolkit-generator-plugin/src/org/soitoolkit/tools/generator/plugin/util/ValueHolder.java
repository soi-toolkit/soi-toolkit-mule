package org.soitoolkit.tools.generator.plugin.util;

/**
 * Simple container that can be used as a final variable but hold volatile objects
 * 
 * @author Magnus Larsson
 *
 * @param <T>
 */
public class ValueHolder<T> {
	public T value = null;
	
	public ValueHolder() {
	}
	
	public ValueHolder(T initialValue) {
		value = initialValue;
	}
}
