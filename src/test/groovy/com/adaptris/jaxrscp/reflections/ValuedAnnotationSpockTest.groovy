package com.adaptris.jaxrscp.reflections

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path

import com.adaptris.jaxrscp.fixtures.ResourceFixture;

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ValuedAnnotationSpockTest extends Specification {

	
	def "Valued annotation gets #annotation value of #value for #method method with #parameters"() {
		given:
		def valued = new ValuedAnnotation(ResourceFixture.getMethod(method, parameters as Class[]), annotation)
		
		expect:
		valued.value().isPresent()
		valued.value().get() == value
		
		where:
		annotation	| method		| parameters		| value
		Consumes	| "xmlReader"	| null				| ["text/xml"]
		Path		| "xmlReader"	| null				| "/xml"
		Path		| "header"		| [String]			| "/header"		
	}
	
	def "Valued annotation value is absent for not existing annotation"() {		
		when:
		def valued = new ValuedAnnotation(ResourceFixture.getMethod("header", String), Consumes)
		
		then:
		valued.value().isPresent() == false
	}
	
	def "Valued annotation value is absent for annotation with empty value"() {
		when:
		def valued = new ValuedAnnotation(ResourceFixture.getMethod("header", String), GET)
		
		then:
		valued.value().isPresent() == false
	}
	
}
