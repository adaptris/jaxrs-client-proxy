package com.adaptris.jaxrscp.reflections;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericType;

import com.adaptris.jaxrscp.NameValuePair;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.reflect.TypeToken;

public class MetaDataReader implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final List<Class<? extends Annotation>> PARAM_ANNOTATIONS = Arrays.asList(PathParam.class,
			QueryParam.class, HeaderParam.class, MatrixParam.class, FormParam.class, BeanParam.class);

	private Optional<String> httpMethod;
	private Optional<String[]> contentType;
	private Optional<String[]> accept;
	private Optional<String> path;
	private Optional<EntityDescription> entityPosition;
	private GenericType responseType;

	private Map<Class<? extends Annotation>, List<NameValuePair<Integer>>> parametersPositions = new HashMap<>();

	private final Method method;
	private final Class<?> clazz;

	MetaDataReader(Class<?> resourceClass, Method method) {
		this.method = method;
		this.clazz = resourceClass;
		List<NameValuePair<Integer>> beanspositions = readPositions(BeanParam.class);
		parametersPositions.put(BeanParam.class, beanspositions);
	}

	/**
	 * @return http method POST, GET, PUT...
	 */
	public Optional<String> readHttpMethod() {
		if (httpMethod == null) {
			if (method.getAnnotations() != null) {
				for (Annotation annotation : method.getAnnotations()) {
					ValuedAnnotation<String> valuedAnnotation = new ValuedAnnotation<String>(
							annotation.annotationType(), HttpMethod.class);
					if (valuedAnnotation.value().isPresent()) {
						httpMethod = valuedAnnotation.value();
						break;
					}
				}
			}
			if (httpMethod == null) {
				httpMethod = Optional.absent();
			}
		}
		return httpMethod;
	}

	public Optional<String> readPath() {
		if (path == null) {
			StringBuilder path = new StringBuilder();

			if (clazz.isAnnotationPresent(Path.class)) {
				Path annotation = clazz.getAnnotation(Path.class);
				path.append(annotation.value());
			}

			if (method.isAnnotationPresent(Path.class)) {
				Path annotation = method.getAnnotation(Path.class);
				path.append(annotation.value());
			}

			if (path.length() == 0) {
				this.path = Optional.absent();
			} else {
				this.path = Optional.of(path.toString());
			}
		}
		return path;
	}

	private <T> Optional<T> readForMethodOrClass(Class<? extends Annotation> annotation) {
		ValuedAnnotation<T> valued = new ValuedAnnotation<>(method, annotation);
		if (valued.value().isPresent()) {
			return valued.value();
		}
		valued = new ValuedAnnotation<>(clazz, annotation);
		return valued.value();
	}

	public Optional<String[]> readContentType() {
		if (contentType == null) {
			contentType = readForMethodOrClass(Consumes.class);
		}
		return contentType;
	}

	public Optional<String[]> readAccept() {
		if (accept == null) {
			accept = readForMethodOrClass(Produces.class);
		}
		return accept;
	}

	private List<NameValuePair<Integer>> readPositions(Class<? extends Annotation> annClass) {
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		List<NameValuePair<Integer>> positions = new ArrayList<>();
		if (parameterAnnotations != null && parameterAnnotations.length > 0) {
			int position = 0;
			for (Annotation[] annotations : parameterAnnotations) {
				Optional<? extends Annotation> annotation = FluentIterable.of(annotations).filter(annClass).first();
				if (annotation.isPresent()) {
					ValuedAnnotation<String> valued = new ValuedAnnotation<>(annotation.get());
					if (valued.value().isPresent()) {
						positions.add(new NameValuePair<Integer>(valued.value().get(), position));
					} else if (annClass.equals(BeanParam.class)) {
						positions.add(new NameValuePair<Integer>(null, position));

					}
				}
				position++;
			}
		}
		return positions;
	}

	private List<NameValuePair<Object>> readValues(List<NameValuePair<Integer>> positions, Object[] args) {
		List<NameValuePair<Object>> values = new ArrayList<>(positions.size());
		if (args != null) {
			for (NameValuePair<Integer> position : positions) {
				Object value = args[position.getValue()];
				if(position.getName() != null){
					values.add(new NameValuePair<Object>(position.getName(), value));
				}
			}
		}
		return values;
	}

	private List<NameValuePair<Object>> readValues(Class<? extends Annotation> annotation, Object[] args) {
		
		List<NameValuePair<Integer>> positions = parametersPositions.get(annotation);
		if (positions == null) {
			positions = readPositions(annotation);
			parametersPositions.put(annotation, positions);
		}

		List<NameValuePair<Object>> headers = readValues(positions, args);
		headers.addAll(readInBeanParams(annotation, args));
		return headers;
	}

	public List<NameValuePair<Object>> readHeaderParams(Object[] args) {
		return readValues(HeaderParam.class, args);
	}

	public List<NameValuePair<Object>> readPathParams(Object[] args) {
		return readValues(PathParam.class, args);
	}

	public List<NameValuePair<Object>> readMatrixParams(Object[] args) {
		return readValues(MatrixParam.class, args);
	}

	public List<NameValuePair<Object>> readQueryParams(Object[] args) {
		return readValues(QueryParam.class, args);
	}

	public List<NameValuePair<Object>> readFormParams(Object[] args) {
		return readValues(FormParam.class, args);
	}

	
	
	
	public List<NameValuePair<Object>> readInBeanParams(Class<? extends Annotation> annotation, Object[] args) {
		List<NameValuePair<Object>> parameters = new ArrayList<>();
		List<NameValuePair<Integer>> positions = parametersPositions.get(BeanParam.class);
		for (NameValuePair<Integer> nameValuePair : positions) {
			Object beanparam = args[nameValuePair.getValue()];
			Field[] fields = MetaDataReader.getAnnotatedDeclaredFields(beanparam.getClass(), annotation, false);
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					ValuedAnnotation<String> va = new ValuedAnnotation<>(field.getAnnotation(annotation));
					if(va.value().isPresent()){
						parameters.add(new NameValuePair<Object>(va.value().get(), field.get(beanparam)));
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				} 
			}
			
		}
		
		return parameters;
	}

	
	private boolean containsParamAnnotation(Annotation[] annotations) {
		if (annotations == null || annotations.length == 0) {
			return false;
		}
		for (Annotation annotation : annotations) {
			if (PARAM_ANNOTATIONS.contains(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}

	public Optional<EntityDescription> readEntity(Object[] args) {
		readEntityPosition();
		if (entityPosition.isPresent()) {
			EntityDescription description = entityPosition.get();
			return Optional.of(new EntityDescription(description.getType(), args[description.getPosition()]));
		}
		return entityPosition;
	}

	private void readEntityPosition() {
		if (entityPosition == null) {
			Annotation[][] parametersAnnotations = method.getParameterAnnotations();
			if (parametersAnnotations != null && parametersAnnotations.length > 0) {
				int position = 0;
				for (Annotation[] annotations : parametersAnnotations) {
					if (!containsParamAnnotation(annotations)) {
						EntityDescription description = new EntityDescription(position,
								method.getParameterTypes()[position]);
						entityPosition = Optional.of(description);
						break;
					}
					position++;
				}
			}
			if (entityPosition == null) {
				entityPosition = Optional.absent();
			}
		}
	}

	public GenericType readResponseType() {
		if (responseType == null) {
			responseType = new GenericType(TypeToken.of(clazz).resolveType(method.getGenericReturnType()).getRawType());
		}
		return responseType;
	}

	public static Field[] getAnnotatedDeclaredFields(@SuppressWarnings("rawtypes") Class clazz, Class<? extends Annotation> annotationClass, boolean recursively) {
		Field[] allFields = getDeclaredFields(clazz, recursively);
		List<Field> annotatedFields = new LinkedList<Field>();
		for (Field field : allFields) {
			if (field.isAnnotationPresent(annotationClass))
				annotatedFields.add(field);
		}

		return annotatedFields.toArray(new Field[annotatedFields.size()]);
	}

	@SuppressWarnings("rawtypes")
	public static Field[] getDeclaredFields(Class clazz, boolean recursively) {
		List<Field> fields = new LinkedList<Field>();
		Field[] declaredFields = clazz.getDeclaredFields();
		Collections.addAll(fields, declaredFields);
		Class superClass = clazz.getSuperclass();
		if (superClass != null && recursively) {
			Field[] declaredFieldsOfSuper = getDeclaredFields(superClass, recursively);
			if (declaredFieldsOfSuper.length > 0)
				Collections.addAll(fields, declaredFieldsOfSuper);
		}
		return fields.toArray(new Field[fields.size()]);
	}
	

}
