package com.adaptris.jaxrscp

import static com.github.tomakehurst.wiremock.client.WireMock.*

import org.junit.Rule

import spock.lang.Specification
import spock.lang.Unroll

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
		def paramHandler = new ParamHandler();
		paramHandler.headerParam = "BeanParam-Header-Value"
		paramHandler.pathParam = "BeanParam-Path-Value"
		paramHandler.queryParam = "BeanParam-Query-Value"
		paramHandler.matrixParam = "BeanParam-Matrix-Value"		
		paramHandler.headerParam2 = "Header 1 value2"
		
		def header1 = "Method Header"
		wireMockRule.stubFor(
			post(urlEqualTo("/api/test/beanParam;matrixParamAV=BeanParam-Matrix-Value?queryParamAV=$paramHandler.queryParam"))
			.willReturn(aResponse().withStatus(200)))

		when:
		client.beanParam(paramHandler, header1)

		then:
		verify(1,
				postRequestedFor(urlEqualTo("/api/test/beanParam;matrixParamAV=BeanParam-Matrix-Value?queryParamAV=$paramHandler.queryParam"))
				.withHeader("Content-Type", equalTo("BeanParam-Header-Value"))
				.withHeader("Header-2", equalTo("Method Header"))
				)
	}
}
