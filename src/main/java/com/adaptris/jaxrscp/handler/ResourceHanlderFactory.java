package com.adaptris.jaxrscp.handler;

import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;

import com.adaptris.jaxrscp.reflections.MetaDataReaderFactory;

public class ResourceHanlderFactory {


	public static <T> T newResource(Class<T> resource, WebTarget target, MultivaluedMap<String, Object> headers) {
		return createResourceInstance(resource, new ResourceHandler(resource, target, headers, new MetaDataReaderFactory()));
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T createResourceInstance(Class<T> resource, ResourceHandler handler) {
		return (T) Proxy.newProxyInstance(
				AccessController.doPrivileged(createPrivil(resource)),
				new Class[] {resource},
				handler);
	}
	
	private static PrivilegedAction<ClassLoader> createPrivil(final Class<?> clazz) {
        return new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return clazz.getClassLoader();
            }
        };
    }	
	
}
