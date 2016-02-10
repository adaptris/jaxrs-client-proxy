package com.adaptris.jaxrscp.reflections

import java.lang.reflect.Method;

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.client.WebTarget;

import com.adaptris.jaxrscp.WebTargetVisitor;
import com.adaptris.jaxrscp.fixtures.ResourceFixture;

import spock.lang.Specification
import spock.lang.Unroll


@Unroll
class MetaDataReaderFactorySpockTest extends Specification {

	
	Class clazz	
	Method mixedParamsMethod
	Method headerMethod
	
	def setup() {
		clazz = ResourceFixture		
		mixedParamsMethod = clazz.getDeclaredMethod("mixedParams", [String, Integer, String, String, String] as Class[])
		headerMethod = clazz.getDeclaredMethod("header", [String] as Class[])
	}
	
	def "Meta data reader factory returns exact same instace of reader for same method and class"() {
		given:
			def factory = new MetaDataReaderFactory();
		when:
			def metaReader1 = factory.readerFor(clazz, mixedParamsMethod)
			def metaReader2 = factory.readerFor(clazz, mixedParamsMethod)
			factory = new MetaDataReaderFactory()
			def metaReader3 = factory.readerFor(clazz, mixedParamsMethod)
			def metaReader4 = factory.readerFor(clazz, headerMethod)
			def metaReader5 = factory.readerFor(clazz, headerMethod)
		then:
			metaReader1 == metaReader2
			metaReader2 == metaReader3
			metaReader1 == metaReader3
			metaReader4 != metaReader1
			metaReader4 != metaReader3
			metaReader5 == metaReader4
	}

}
