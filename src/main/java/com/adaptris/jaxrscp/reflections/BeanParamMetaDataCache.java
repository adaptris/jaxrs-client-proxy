package com.adaptris.jaxrscp.reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class BeanParamMetaDataCache {
	private static LoadingCache<BeanParamMetaKey, List<Field>> cache;

	public List<Field> search(Class<?> clazz, Class<? extends Annotation> annotation) throws ExecutionException {
		List<Field> instance = getCacheInstance().get(new BeanParamMetaKey(clazz, annotation));
		return instance;
	}

	
	private static LoadingCache<BeanParamMetaKey, List<Field>> getCacheInstance() {
		if (cache == null) {
			cache = CacheBuilder.newBuilder().build(annotatedDeclaredFieldsLoader());
		}
		return cache;
	}

	
	/**
	 * Guava cache loader
	 */
	private static CacheLoader<BeanParamMetaKey, List<Field>> annotatedDeclaredFieldsLoader() {
		return new CacheLoader<BeanParamMetaKey, List<Field>>() {
			@Override
			public List<Field> load(BeanParamMetaKey key) throws Exception {
				return MetaDataReader.getAnnotatedDeclaredFields(key.clazz, key.annotation, true);
			}
		};
	}

	/**
	 * Cache key class
	 */
	public class BeanParamMetaKey {
		public BeanParamMetaKey(Class<?> clazz, Class<? extends Annotation> annotation) {
			this.clazz = clazz;
			this.annotation = annotation;
		}

		Class<?> clazz;
		Class<? extends Annotation> annotation;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BeanParamMetaKey other = (BeanParamMetaKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (annotation == null) {
				if (other.annotation != null)
					return false;
			} else if (!annotation.equals(other.annotation))
				return false;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			return true;
		}

		private BeanParamMetaDataCache getOuterType() {
			return BeanParamMetaDataCache.this;
		}

	}

}
