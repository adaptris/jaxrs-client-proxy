package com.adaptris.jaxrscp.handler;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by irlap on 04/11/2016.
 */
public interface InvocationHandlerInterceptor {

    InterceptResponse beforeCall(ResourceHandler handler, Object proxy, Method method, Object[] args, MultivaluedMap<String, Object> headers, Map<String, Object> callContext);

    InterceptResponse afterCall(ResourceHandler handler, Object proxy, Method method, Object[] args, Object response, MultivaluedMap<String, Object> headers, Map<String, Object> callContext);

    InterceptResponse onCallException(Throwable ex, ResourceHandler resourceHandler, Object proxy, Method method, Object[] args, MultivaluedMap<String, Object> headers, Map<String, Object> callContext) throws Throwable;


}
