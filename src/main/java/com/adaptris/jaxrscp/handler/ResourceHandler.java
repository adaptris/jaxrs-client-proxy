package com.adaptris.jaxrscp.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.adaptris.jaxrscp.NameValuePair;
import com.adaptris.jaxrscp.WebTargetVisitor;
import com.adaptris.jaxrscp.reflections.EntityDescription;
import com.adaptris.jaxrscp.reflections.MetaDataReader;
import com.adaptris.jaxrscp.reflections.MetaDataReaderFactory;
import com.google.common.base.Optional;

public class ResourceHandler implements InvocationHandler{
	
	private final WebTarget target;
	private final MultivaluedMap<String, Object> headers;
	private final Class<?> resourceClass;
	private final MetaDataReaderFactory factory;

	ResourceHandler(Class<?> resourceClass, WebTarget target, MultivaluedMap<String, Object> headers, MetaDataReaderFactory factory) {
		this.target = target;
		this.headers = headers;
		this.resourceClass = resourceClass;
		this.factory = factory;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MetaDataReader reader = this.factory.readerFor(this.resourceClass, method);
		WebTargetVisitor visitor = new WebTargetVisitor(reader);
		WebTarget target = visitor.visit(this.target, args);
		
		Builder request = target.request();
		request.headers(readHeaders(reader, args));
		
		String httpMethod = reader.readHttpMethod().or("GET");
		Optional<EntityDescription> entityDescription = reader.readEntity(args);
		Object result;
		if (entityDescription.isPresent()) {
			EntityDescription description = entityDescription.get();
			Object entity = description.getValue();
			if (description.getType() instanceof ParameterizedType) {				
				entity = new GenericEntity(entity, description.getType());
			}
            result = request.method(httpMethod, Entity.entity(entity, reader.readContentType().get()[0]), reader.readResponseType());
		} else {
			result = request.method(httpMethod, reader.readResponseType());
		}		
		return result;
	}
	
	private MultivaluedMap<String, Object> readHeaders(MetaDataReader reader, Object[] args) {
		MultivaluedHashMap<String, Object> headers;
		if (this.headers != null) {
			headers = new MultivaluedHashMap<String, Object>(this.headers);
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
