package com.adaptris.jaxrscp.handler;

import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;

public class ResourceHandlerFactory {


	public static <T> T newResource(Class<T> resource, WebTarget target, MultivaluedMap<String, Object> headers, List<InvocationHandlerInterceptor> listeners) {
		return createResourceInstance(resource, new ResourceHandler(resource, target, headers, listeners));
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T createResourceInstance(Class<T> resource, ResourceHandler handler) {
		return (T) Proxy.newProxyInstance(
				AccessController.doPrivileged(createPrivileged(resource)),
				new Class[] {resource},
				handler);
	}
	
	private static PrivilegedAction<ClassLoader> createPrivileged(final Class<?> clazz) {
        return new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return clazz.getClassLoader();
            }
        };
    }	
	
}
