package com.adaptris.jaxrscp.reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Optional;

/**
 * Read value() of annotation not knowing exact type of it.
 * Apparently even 95% annotations in java have value() attribute there is no easy way
 * to get it not having the exact type of annotation annotation.
 * So this class use reflection to execute the value() of annotation. In case there is no value empty Option is returned.
 * 
 * @author irla
 *
 * @param T type of value - usually String
 */
public class ValuedAnnotation<T> {
	
	private final Annotation annotation;
	private Optional<T> value;

	public ValuedAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
	
	public ValuedAnnotation(AnnotatedElement element, Class<? extends Annotation> annotation) {
		this(element.getAnnotation(annotation));
	}
	
	@SuppressWarnings("unchecked")
	Optional<T> value() {
		if (value == null) {
			if (annotation != null) {
				try {
					Method method = annotation.annotationType().getMethod("value", (Class[])null);
					value = Optional.of((T)method.invoke(annotation, (Object[]) null));
				} catch (NoSuchMethodException e ) {
					value = Optional.absent(); //No value in this annotation
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
			if (value == null) {
				value = Optional.absent();
			}
		}
		return value;
	}
}
