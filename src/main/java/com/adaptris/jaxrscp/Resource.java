package com.adaptris.jaxrscp;

import javax.ws.rs.client.Client;

public class Resource<T> implements AutoCloseable {

	private final T t;
	private final Client client;

	public Resource(T t, Client client) {
		this.t = t;
		this.client = client;		
	}
	
	public T get() {
		return t;
	}
	
	@Override
	public void close() throws Exception {
		client.close();
	}
}
