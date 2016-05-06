package com.adaptris.jaxrscp;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

import com.adaptris.jaxrscp.reflections.MetaDataReader;
import com.google.common.base.Optional;

public class WebTargetVisitor {
	
	private final MetaDataReader reader;

	public WebTargetVisitor(MetaDataReader reader) {
		this.reader = reader;
	}
	
	public WebTarget visit(WebTarget target, Object[] methodArgs) {
		target = visitPath(target);
		target = visitPathParams(target, methodArgs);
		target = visitQueryParams(target, methodArgs);
		target = visitMatrixParams(target, methodArgs);
		return target;
	}

	private WebTarget visitPath(WebTarget target) {
		Optional<String> path = this.reader.readPath();
		if (path.isPresent()) {
			target = target.path(path.get());
		}
		return target;
	}
	
	private WebTarget visitPathParams(WebTarget target, Object[] methodArgs) {
		List<NameValuePair<Object>> readPathParams = reader.readPathParams(methodArgs);
		for (NameValuePair<Object> pair : readPathParams) {
			target = resolveTemplate(target, pair);
		}
		return target;
	}

	private WebTarget resolveTemplate(WebTarget target, NameValuePair<Object> pair) {
		if(PathValueReader.isPathSegment(pair.getValue())){
			target = target.resolveTemplateFromEncoded(pair.getName(), PathValueReader.read(pair.getValue()));
		}
		else{
			target = target.resolveTemplate(pair.getName(), pair.getValue());
		}
		return target;
	}
	
	private WebTarget visitQueryParams(WebTarget target, Object[] methodArgs) {
		List<NameValuePair<Object>> readQueryParams = reader.readQueryParams(methodArgs);
		for (NameValuePair<Object> pair : readQueryParams) {
			target = target.queryParam(pair.getName(), collectionToArray(pair.getValue()));
		}
		return target;
	}
	
	private WebTarget visitMatrixParams(WebTarget target, Object[] methodArgs) {
		List<NameValuePair<Object>> readMatrixParams = reader.readMatrixParams(methodArgs);
		for (NameValuePair<Object> pair : readMatrixParams) {
			target = target.matrixParam(pair.getName(), collectionToArray(pair.getValue()));
		}
		return target;
	}
	
	private Object collectionToArray(Object obj) {
		if (obj instanceof Collection) {
			return ((Collection) obj).toArray();
		}
		return obj;
	}

	public Optional<Form> readForm(Object[] args) {
		List<NameValuePair<Object>> readFormParams = this.reader.readFormParams(args);
		if (! readFormParams.isEmpty()) {
			Form form = new Form();
			for (NameValuePair<Object> nameValuePair : readFormParams) {
				String value = null;
				if (nameValuePair.getValue() != null) {
					value = nameValuePair.getValue().toString();
				}
				form.param(nameValuePair.getName(), value);
			}
			return Optional.of(form);
		}
		return Optional.absent();
	}

}
