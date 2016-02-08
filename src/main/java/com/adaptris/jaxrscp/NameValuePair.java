package com.adaptris.jaxrscp;

public class NameValuePair<T> {

	private final String name;
	private final T value;
	
	public NameValuePair(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public T getValue() {
		return value;
	}
	
}
