package com.adaptris.jaxrscp

import static com.github.tomakehurst.wiremock.client.WireMock.*

import org.junit.Rule

import spock.lang.Specification
import spock.lang.Unroll

import com.adaptris.jaxrscp.fixtures.Parameter;
import com.adaptris.jaxrscp.fixtures.ResourceFixture
import com.github.tomakehurst.wiremock.junit.WireMockRule

@Unroll
class ResoruceBeanParamFixtureSpockIntegrationTest extends Specification{

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

	def "It handles BeanParam parameters mixed with header param argument" () {
		given:
			def parameter = new Parameter();
			parameter.headerParam = "BeanParam-Header-Value"
			parameter.pathParam = "BeanParam-Path-Value"
			parameter.queryParam = "BeanParam-Query-Value"
			parameter.matrixParam = "BeanParam-Matrix-Value"		
			parameter.headerParam2 = "Header 1 value2"
			
			def header1 = "Method Header"
			wireMockRule.stubFor(
				post(urlEqualTo("/api/test/beanParam;matrixParamAV=BeanParam-Matrix-Value?queryParamAV=$parameter.queryParam"))
				.willReturn(aResponse().withStatus(200)))

		when:
			client.beanParam(parameter, header1)

		then:
			verify(1,
				postRequestedFor(urlEqualTo("/api/test/beanParam;matrixParamAV=BeanParam-Matrix-Value?queryParamAV=$parameter.queryParam"))
				.withHeader("Content-Type", equalTo("BeanParam-Header-Value"))
				.withHeader("Header-2", equalTo("Method Header"))
				)
	}
	
	def "If the same param is set in method and Bean then take method param" () {
		given:
			def parameter = new Parameter();
			parameter.headerParam2 = "Bean Param Header2"
			
			def header = "Method Header"
			wireMockRule.stubFor(
				post(urlEqualTo("/api/test/beanParam"))
				.willReturn(aResponse().withStatus(200)))

		when:
			client.beanParam(parameter, header)

		then:
			verify(1,
				postRequestedFor(urlEqualTo("/api/test/beanParam"))
				.withHeader("Header-2", equalTo("Method Header"))
				)
	}
}
