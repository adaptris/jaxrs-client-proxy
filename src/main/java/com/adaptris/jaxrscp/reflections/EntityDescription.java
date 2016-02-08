package com.adaptris.jaxrscp.reflections;

import java.lang.reflect.Type;

public class EntityDescription {

	private final Integer position;
	private final Type type;
	private final Object value;

	public EntityDescription(Integer position, Type type) {
		this.position = position;
		this.type = type;
		this.value = null;
	}
	
	public EntityDescription(Type type, Object value) {
		this.position = null;
		this.type = type;
		this.value = value;
	}

	public Integer getPosition() {
		return position;
	}

	public Type getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

}
