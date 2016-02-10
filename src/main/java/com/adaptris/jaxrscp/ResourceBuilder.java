package com.adaptris.jaxrscp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;

import com.adaptris.jaxrscp.handler.ResourceHanlderFactory;
import com.google.common.net.HttpHeaders;


public class ResourceBuilder {	

	private String url;
	private MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
	
	public ResourceBuilder url(String url) {
		this.url = url;
		return this;
	}

	public ResourceBuilder bearerAccessToken(String token) {
		Object accessTokenHeader = "Bearer " + token;
		return putHeader(HttpHeaders.AUTHORIZATION, accessTokenHeader);
	}
	
	/**
	 * Adds to existing or creates header
	 * @param header
	 * @param value
	 * @return
	 */
	public ResourceBuilder addHeader(String header, Object value) {
		List<Object> list = headers.get(header);
		if (list == null) {
			list = new ArrayList<>();
			headers.put(header, list);
		}
		list.add(value);
		return this;
	}
	
	public ResourceBuilder putHeader(String header, Object value) {
		headers.put(header, new ArrayList<>(Arrays.asList(value)));
		return this;
	}
	
	public <T> Resource<T> build(Class<T> clazz){
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(url);
		T t = ResourceHanlderFactory.newResource(clazz, target, headers);
		return new Resource<T>(t, client);
		
	}
	
}
