package com.adaptris.jaxrscp

import static com.github.tomakehurst.wiremock.client.WireMock.*

import javax.ws.rs.core.MediaType

import org.junit.Rule

import spock.lang.Specification
import spock.lang.Unroll

import com.adaptris.jaxrscp.fixtures.ParameterWithFormParams;
import com.adaptris.jaxrscp.fixtures.ResourceFixture
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.common.net.HttpHeaders

@Unroll
class ResourceFixtureSpockIntegrationTest extends Specification{
	
	private static final PORT = 8089
	
	@Rule
	WireMockRule wireMockRule = new WireMockRule(PORT);
	
	Resource resource
	ResourceFixture client
	
	def setup() {
		def builder = new ResourceBuilder();
		resource = builder
			.url("http://localhost:$PORT/api")
			.build(ResourceFixture)
		client = resource.get()
	}
	
	def cleanup() {
		resource.close()
	}
	
	def "It calls correctly the get method"() {
		given:
		def id = 7
		wireMockRule.stubFor(
			get(urlEqualTo("/api/test/$id"))
			.willReturn(
				aResponse()
				.withStatus(200)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("response")
			)
		)		
		expect:
		client.get(id) == "response"
	}
	
	def "It handles entities in put method"() {
		given:
		def id = 7
		wireMockRule.stubFor(
			put(urlEqualTo("/api/test/$id"))
			.withRequestBody(matching(".*"))
			.willReturn(
				aResponse()
				.withStatus(200)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("Response for PUT and Entity")
			)
		)
		
		expect:
		client.update(id, "String Entity") == "Response for PUT and Entity"
	}
	
	def "It handles more complex methods" () {
		given:
		def path1 = 42
		def header1 = "Header 1 value"
		def header2 = "Header 2 value"
		def matrix1 = "Matrix_1_Value"
		def query1 = "Query_1_value" 
		wireMockRule.stubFor(
			get(urlEqualTo("/api/test/mixedParams/$path1;matrix1=$matrix1?query1=$query1"))
			.willReturn(
				aResponse()
				.withStatus(200)
			)
		)
		
		when:
		client.mixedParams(header1, path1, query1, matrix1, header2)
		
		then:
		verify(1, 
			getRequestedFor(urlEqualTo("/api/test/mixedParams/$path1;matrix1=$matrix1?query1=$query1"))
			.withHeader("Header-1", equalTo(header1))
			.withHeader("Header-2", equalTo(header2))
		)
	}
	
	def "Many methods can be called for one resource client"() {
		given:
		def mixedUrl = "/api/test/mixedParams/42;matrix1=matrix1Value?query1=query1Value"
		wireMockRule.stubFor(
			get(urlEqualTo(mixedUrl))
			.willReturn(aResponse())
		)
		def getPutUrl = "/api/test/42"
		wireMockRule.stubFor(
			get(urlEqualTo(getPutUrl))
			.willReturn(aResponse()
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("get_response"))
		)
		wireMockRule.stubFor(
			put(urlEqualTo(getPutUrl))			
			.withRequestBody(equalTo("put body"))
			.willReturn(aResponse()
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("put_response"))
		)
		
		when:
		client.mixedParams("Header 1", 42, "query1Value", "matrix1Value", "Header 2")
		def getResponse = client.get(42)
		def putResponse = client.update(42, "put body")
		
		then:
		getResponse == "get_response"
		putResponse == "put_response"
		verify(1,
			getRequestedFor(urlEqualTo(mixedUrl))
			.withHeader("Header-1", equalTo("Header 1"))
			.withHeader("Header-2", equalTo("Header 2"))
		)
		verify(1,
			getRequestedFor(urlEqualTo(getPutUrl))
		)
		verify(1,
			putRequestedFor(urlEqualTo(getPutUrl))
			.withRequestBody(equalTo("put body"))
		)
	}
	
	def "Custom user header can be provided"() {
		given:
		ResourceBuilder builder = new ResourceBuilder();
		Resource resource = builder
			.url("http://localhost:$PORT/api")
			.bearerAccessToken("fake-access-token")
			.addHeader(HttpHeaders.AUTHORIZATION, "another")
			.putHeader("Custom", "custom")
			.addHeader("Custom-1", "a")
			.addHeader("Custom-1", "b")
			.build(ResourceFixture)
			
		client = resource.get();
		
		wireMockRule.stubFor(
			get(urlEqualTo("/api/test/42"))
			.willReturn(aResponse()
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("get_response"))
		)
		
		when:
		def response = client.get(42)
		
		then:
		response == "get_response"
		verify(1,
			getRequestedFor(urlEqualTo("/api/test/42"))
			.withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer fake-access-token,another"))
			.withHeader("Custom", equalTo("custom"))
			.withHeader("Custom-1", equalTo("a,b"))
		)
			
	}
	
	def "Form params read from method attributes are supported"() {
		given:
		ResourceBuilder builder = new ResourceBuilder();
		Resource resource = builder
			.url("http://localhost:$PORT/api")
			.build(ResourceFixture)
			
		client = resource.get();
		
		wireMockRule.stubFor(
			post(urlEqualTo("/api/test/mixedFormParams/33"))
				.willReturn(aResponse())
		)
		
		when:
		client.mixedFormParams("Form 1", 33, "Form 2")
		
		then:
		verify(1,
			postRequestedFor(urlEqualTo("/api/test/mixedFormParams/33"))
			.withRequestBody(equalTo("form1=Form+1&form2=Form+2"))
		)
	}
	
	def "Form params read from beanparam are supported"() {
		given:
		ResourceBuilder builder = new ResourceBuilder();
		Resource resource = builder
			.url("http://localhost:$PORT/api")
			.build(ResourceFixture)
			
		client = resource.get();
		
		wireMockRule.stubFor(
			post(urlMatching("/api/test/beanParamWithFormParams"))
				.willReturn(aResponse())
		)
		
		def beanParam = new ParameterWithFormParams(
			formParamA: "Form-param-a",
			formParamB: "Form-param-b",
			formParamC: "Form-param-c",			
			headerParam: "HeaderParam"
		)
		
		when:
		client.beanParamWithFormParams(beanParam, "Another header parameter")
		
		then:
		verify(1,
			postRequestedFor(urlEqualTo("/api/test/beanParamWithFormParams"))
			.withHeader("Header-2", equalTo("Another header parameter"))
			.withRequestBody(equalTo("paramA=Form-param-a&paramB=Form-param-b&paramC=Form-param-c"))
		)
	}

}
