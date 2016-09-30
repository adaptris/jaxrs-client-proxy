package com.adaptris.jaxrscp.reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adaptris.jaxrscp.NameValuePair;

public class BeanParamValueSearcher {
	Logger logger = Logger.getLogger(BeanParamValueSearcher.class.toString());
	
	private static BeanParamMetaDataCache cache = null;

	public Collection<? extends NameValuePair<Object>> read(Class<? extends Annotation> annotation, Object[] args, List<NameValuePair<Integer>> positions) {
		List<NameValuePair<Object>> parameters = new ArrayList<>();
		for (NameValuePair<Integer> param : positions) {
			Object beanparam = args[param.getValue()];
			readValuesFromBeanParamArgument(annotation, beanparam, parameters);
		}
		return parameters;
	}

	public void setCache(BeanParamMetaDataCache beanParamfactory) {
		if (BeanParamValueSearcher.cache == null) {
			BeanParamValueSearcher.cache = beanParamfactory;
		}
	}
	
	

	private void readValuesFromBeanParamArgument(Class<? extends Annotation> annotation, Object beanparam, List<NameValuePair<Object>> parameters) { 
		try {
			List<Field>	fields = cache.search(beanparam.getClass(), annotation);
			for (Field field : fields) {
				field.setAccessible(true);
				ValuedAnnotation<String> va = new ValuedAnnotation<>(field.getAnnotation(annotation));
				if (va.value().isPresent() && field.get(beanparam) != null) {
					parameters.add(new NameValuePair<Object>(va.value().get(), field.get(beanparam)));
				}

			}
		} catch (ExecutionException | IllegalArgumentException | IllegalAccessException e) {
			logger.log(Level.SEVERE,"Bean Param meta data read failed ", e);
		}
	}
}
