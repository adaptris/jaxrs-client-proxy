package com.adaptris.jaxrscp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;

import com.adaptris.jaxrscp.handler.InvocationHandlerInterceptor;
import com.adaptris.jaxrscp.handler.ResourceHandlerFactory;
import com.google.common.net.HttpHeaders;


public class ResourceBuilder {

    private String url;
    private MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    private List<Class<?>> registerClasses = new ArrayList<>();
    private List<Object> registerObjects = new ArrayList<>();
    private List<InvocationHandlerInterceptor> listeners = new ArrayList<>();

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
     *
     * @param header header name
     * @param value  value of header
     * @return this
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

    public void register(Class<?> register) {
        registerClasses.add(register);
    }

    public void register(Object register) {
        registerObjects.add(register);
    }

    public void register(InvocationHandlerInterceptor listener) {
        listeners.add(listener);
    }

    public <T> Resource<T> build(Class<T> clazz) {
        Client client = ClientBuilder.newClient();
        for (Class<?> registerClass : registerClasses) {
            client.register(registerClass);
        }
        for (Object registerObject : registerObjects) {
            client.register(registerObject);
        }
        WebTarget target = client.target(url);
        T t = ResourceHandlerFactory.newResource(clazz, target, headers, listeners);
        return new Resource<T>(t, client);

    }

}
