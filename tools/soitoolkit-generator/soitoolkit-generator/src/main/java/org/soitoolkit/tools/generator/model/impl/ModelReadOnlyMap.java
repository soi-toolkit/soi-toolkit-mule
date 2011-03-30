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
package org.soitoolkit.tools.generator.model.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.soitoolkit.tools.generator.model.IModel;

public class ModelReadOnlyMap implements Map<String, Object> {

	private IModel model;

	public ModelReadOnlyMap(IModel model) {
		this.model = model;
	}

	public void clear() {
		throw new RuntimeException("Method not implemented");		
	}

	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	public boolean containsValue(Object value) {
		throw new RuntimeException("Method not implemented");		
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new RuntimeException("Method not implemented");		
	}

	public Object get(Object key) {
		if (key == null) return null;
		
		return model.resolveParameter(key.toString(), null);
	}

	public boolean isEmpty() {
		return false;
	}

	public Set<String> keySet() {
		throw new RuntimeException("Method not implemented");		
	}

	public Object put(String key, Object value) {
		throw new RuntimeException("Method not implemented");		
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		throw new RuntimeException("Method not implemented");		
	}

	public Object remove(Object key) {
		throw new RuntimeException("Method not implemented");		
	}

	public int size() {
		throw new RuntimeException("Method not implemented");		
	}

	public Collection<Object> values() {
		throw new RuntimeException("Method not implemented");		
	}
}