package com.adaptris.jaxrscp.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.adaptris.jaxrscp.NameValuePair;
import com.adaptris.jaxrscp.WebTargetVisitor;
import com.adaptris.jaxrscp.reflections.EntityDescription;
import com.adaptris.jaxrscp.reflections.MetaDataReader;
import com.adaptris.jaxrscp.reflections.MetaDataReaderFactory;
import com.google.common.base.Optional;

public class ResourceHandler implements InvocationHandler {

    private final WebTarget target;
    private final MultivaluedMap<String, Object> headers;
    private final Class<?> resourceClass;
    private final MetaDataReaderFactory factory;
    private List<InvocationHandlerInterceptor> listeners;

    ResourceHandler(Class<?> resourceClass, WebTarget target, MultivaluedMap<String, Object> headers, List<InvocationHandlerInterceptor> listeners) {
        this.target = target;
        this.headers = headers;
        this.resourceClass = resourceClass;
        this.listeners = listeners;
        if (this.listeners == null) {
            this.listeners = new ArrayList<>();
        }
        this.factory = new MetaDataReaderFactory();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, Object> callContext = new HashMap<>();
        for (InvocationHandlerInterceptor listener : listeners) {
            InterceptResponse resp = listener.beforeCall(this, proxy, method, args, this.headers, callContext);
            if (resp != null && !resp.isProceed()) {
                return resp.getResponse();
            }
        }
        try {
            Object response = call(method, args, this.headers);
            for (InvocationHandlerInterceptor listener : listeners) {
                InterceptResponse resp = listener.afterCall(this, proxy, method, args, response, this.headers, callContext);
                if (resp != null && !resp.isProceed()) {
                    return resp.getResponse();
                }
            }
            return response;
        } catch (Throwable th) {
            for (InvocationHandlerInterceptor listener : listeners) {
                InterceptResponse resp = listener.onCallException(th, this, proxy, method, args, this.headers, callContext);
                if (resp != null && !resp.isProceed()) {
                    return resp.getResponse();
                }
            }
            throw th;
        }
    }

    public Object call(Method method, Object[] args, MultivaluedMap<String, Object> headers) throws Throwable {
        MetaDataReader reader = this.factory.readerFor(this.resourceClass, method);
        WebTargetVisitor visitor = new WebTargetVisitor(reader);
        WebTarget target = visitor.visit(this.target, args);

        Builder request = target.request();
        request.headers(readHeaders(reader, args, headers));

        String httpMethod = reader.readHttpMethod().or("GET");
        Optional<Form> form = visitor.readForm(args);
        Optional<EntityDescription> entityDescription = reader.readEntity(args);
        Object result;
        if (entityDescription.isPresent() || form.isPresent()) {
            Object entity;
            if (form.isPresent()) {
                entity = form.get();
            } else {
                EntityDescription description = entityDescription.get();
                entity = description.getValue();
                if (description.getType() instanceof ParameterizedType) {
                    entity = new GenericEntity(entity, description.getType());
                }
            }
            result = request.method(httpMethod, Entity.entity(entity, readContentType(reader)), reader.readResponseType());
        } else {
            result = request.method(httpMethod, reader.readResponseType());
        }
        return result;
    }

    private String readContentType(MetaDataReader reader) {
        Optional<Object> header = Optional.fromNullable(this.headers.getFirst(HttpHeaders.CONTENT_TYPE));
        if (header.isPresent()) return (String) header.get();

        return reader.readContentType().get()[0];
    }

    private MultivaluedMap<String, Object> readHeaders(MetaDataReader reader, Object[] args, MultivaluedMap<String, Object> extraHeaders) {
        MultivaluedHashMap<String, Object> headers;
        if (extraHeaders != null) {
            headers = new MultivaluedHashMap<String, Object>(extraHeaders);
        } else {
            headers = new MultivaluedHashMap<>();
        }

        List<NameValuePair<Object>> headerParams = reader.readHeaderParams(args);
        for (NameValuePair<Object> header : headerParams) {
            if (headers.get(header.getName()) == null) { //Only set header if not previously defined
                headers.add(header.getName(), header.getValue());
            }
        }

        return headers;
    }
}
