package org.soitoolkit.tools.generator.plugin.model.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.soitoolkit.tools.generator.plugin.model.IModel;

public class ModelReadOnlyMap implements Map<String, Object> {

	private IModel model;

	public ModelReadOnlyMap(IModel model) {
		this.model = model;
	}

	@Override
	public void clear() {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public Object get(Object key) {
		if (key == null) return null;
		
		return model.resolveParameter(key.toString(), null);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Set<String> keySet() {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public Object put(String key, Object value) {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public Object remove(Object key) {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public int size() {
		throw new RuntimeException("Method not implemented");		
	}

	@Override
	public Collection<Object> values() {
		throw new RuntimeException("Method not implemented");		
	}
}
