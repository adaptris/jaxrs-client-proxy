package com.adaptris.jaxrscp.reflections

import javax.ws.rs.HttpMethod

import com.adaptris.jaxrscp.fixtures.ResourceFixture;

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetaDataReaderSpockTest extends Specification {	

	
	def "Reader reads http #annotation for #method method with #parameters"() {
		given:
		def reader = new MetaDataReaderAssert(ResourceFixture, method, parameters)
		
		expect:		
		reader.get().readHttpMethod().orNull() == annotation
		
		where:
		method						| parameters		| annotation
		"xmlReader"					| null				| HttpMethod.POST
		"defaultMethodShouldBeGET"	| null				| null	//optional absent
		"create"					| [Object]			| HttpMethod.POST
		"get"						| [Object]			| HttpMethod.GET
		"update"					| [Object, Object]	| HttpMethod.PUT
		"remove"					| [Object]			| HttpMethod.DELETE
	}
	
	def "Reader reads #path path for #method with #parameters"() {
		given:
		def reader = new MetaDataReaderAssert(ResourceFixture, method, parameters)
		
		expect:
		reader.get().readPath().get() == path
		
		where:
		method						| parameters		| path
		"xmlReader"					| null				| "/test/xml"
		"defaultMethodShouldBeGET"	| null				| "/test/defaultMethodShouldBeGET"
		"create"					| [Object]			| "/test"
		"get"						| [Object]			| "/test/{id}"
		"update"					| [Object, Object]	| "/test/{id}"		
		"remove"					| [Object]			| "/test/{id}"		
	}
	
	def "Reader reads Content-Type #contentType header for #method with #parameters"() {
		given:
		def reader = new MetaDataReaderAssert(ResourceFixture, method, parameters)
		
		expect:
		reader.get().readContentType().isPresent()
		reader.get().readContentType().get() == contentType
		
		where:
		method						| parameters		| contentType
		"xmlReader"					| null				| ["text/xml"] as String[]
		"header"					| [String]			| ["application/xml", "application/json", "text/xml"] as String[]
		"get"						| [Object]			| ["application/xml", "application/json", "text/xml"] as String[]
	}
	
	
	
	def "Reader reads Accept #accept header for #method with #parameters"() {
		given:
		def reader = new MetaDataReaderAssert(ResourceFixture, method, parameters)
		
		expect:
		reader.get().readAccept().isPresent()
		reader.get().readAccept().get() == accept
		
		where:
		method						| parameters		| accept
		"xmlReader"					| null				| ["text/xml"] as String[]
		"header"					| [String]			| ["application/xml", "application/json"] as String[]
		"get"						| [Object]			| ["application/xml", "application/json"] as String[]
	}
	
	def "Reader reads header param value of parameters"() {
		given:
		def reader = new MetaDataReaderAssert(ResourceFixture, "header", [String])
		
		when:
		def headers = reader.get().readHeaderParams(["test"] as Object[])
		
		then:
		headers.size() == 1
		headers.first().name == "Test-Header"
		headers.first().value == "test"
	}
	
	def "Reader reads many param types for parameters"() {
		given:
		def readerAssert = new MetaDataReaderAssert(ResourceFixture, "mixedParams", [String, Integer, String, String, String])
		def reader = readerAssert.get()
		def params = ["header1Value", 42, "query1Value", "matrix1Value", "header2Value"] as Object[]
		
		when:
		def headerParams = reader.readHeaderParams(params)
		def pathParams = reader.readPathParams(params)
		def queryParams = reader.readQueryParams(params)
		def matrixParams = reader.readMatrixParams(params)
		
		then:
		headerParams.size() == 2
		headerParams[0].name == "Header-1"
		headerParams[0].value == "header1Value"
		headerParams[1].name == "Header-2"
		headerParams[1].value == "header2Value"
		
		pathParams.size() == 1
		pathParams[0].name == "path1"
		pathParams[0].value == 42
		
		queryParams.size() == 1
		queryParams[0].name == "query1"
		queryParams[0].value == "query1Value"
		
		matrixParams.size() == 1
		matrixParams[0].name == "matrix1"
		matrixParams[0].value == "matrix1Value"
	}

}
