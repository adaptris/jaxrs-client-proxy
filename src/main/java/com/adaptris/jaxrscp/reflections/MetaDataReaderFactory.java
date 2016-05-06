package com.adaptris.jaxrscp.reflections;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class MetaDataReaderFactory {

	private static LoadingCache<MetaDataReaderKey, MetaDataReader> cache;
	private static BeanParamMetaDataCache beanParamMetaDataCache = null;

	public MetaDataReaderFactory() {
		MetaDataReaderFactory.beanParamMetaDataCache = new BeanParamMetaDataCache();
	}
	
	public MetaDataReader readerFor(Class<?> clazz, Method method) throws ExecutionException {
		return getCacheInstance().get(new MetaDataReaderKey(clazz, method));
	}

	private static LoadingCache<MetaDataReaderKey, MetaDataReader> getCacheInstance() {
		if (cache == null) {
			cache = CacheBuilder.newBuilder()
					// We could put some options for idle and maximum size but we want it all to resist in memory as amount of data stored should be small
					.build(new CacheLoader<MetaDataReaderKey, MetaDataReader>() {
						@Override
						public MetaDataReader load(MetaDataReaderKey key) throws Exception {
							return new MetaDataReader(key.clazz, key.method, beanParamMetaDataCache);
						}
					});
		}
		return cache;
	}

	
	
	/**
	 * Cache Key Class
	 */
	private static final class MetaDataReaderKey {
		final Class<?> clazz;
		final Method method;

		public MetaDataReaderKey(Class<?> clazz, Method method) {
			this.clazz = clazz;
			this.method = method;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = prime * clazz.hashCode();
			result = prime * result + method.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MetaDataReaderKey)) {
				return false;
			}
			MetaDataReaderKey other = (MetaDataReaderKey) obj;
			if (!method.equals(other.method)) {
				return false;
			}
			return clazz.equals(other.clazz);
		}

	}

}
