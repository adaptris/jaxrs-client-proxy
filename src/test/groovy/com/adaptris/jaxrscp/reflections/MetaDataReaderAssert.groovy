package com.adaptris.jaxrscp.reflections

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import com.google.common.base.Optional;

class MetaDataReaderAssert {
	
	private MetaDataReader reader
	
	def MetaDataReaderAssert(def clazz, String methodName, List<Class> parameters) {
		def params = null;
		if (parameters != null) {
			params = parameters as Class[]
		}
		reader = new MetaDataReader(clazz, findMethod(clazz, methodName, params).get())
	}
	
	def get() { reader } 

	def Optional<Method> findMethod(Class<?> clazz, String name, Class<?> ... parameters) {
		try {
			return Optional.of(clazz.getDeclaredMethod(name, parameters))
		} catch (NoSuchMethodException e) {
			if (clazz.getInterfaces() != null) {
				for (Class<?> interf : clazz.getInterfaces()) {
					try {
						return Optional.of(interf.getDeclaredMethod(name, parameters))
					} catch (NoSuchMethodException e1) {}
				}
			}
		}
		return Optional.absent()
	}
	
}
