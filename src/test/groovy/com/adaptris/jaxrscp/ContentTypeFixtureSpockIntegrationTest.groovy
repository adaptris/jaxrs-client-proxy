package com.adaptris.jaxrscp

import static com.github.tomakehurst.wiremock.client.WireMock.*

import javax.ws.rs.core.MediaType

import org.junit.Rule

import spock.lang.Specification
import spock.lang.Unroll
import wiremock.org.apache.http.entity.ContentType;

import com.adaptris.jaxrscp.fixtures.ResourceFixture
import com.github.tomakehurst.wiremock.junit.WireMockRule

@Unroll
class ContentTypeFixtureSpockIntegrationTest extends Specification{

	private static final PORT = 8089

	@Rule
	WireMockRule wireMockRule = new WireMockRule(PORT);

	Resource resource
	ResourceFixture client

	def setup() {
		def builder = new ResourceBuilder();
		resource = builder
				.url("http://localhost:$PORT/api")
				.addHeader("Content-Type", MediaType.APPLICATION_JSON)
				.addHeader("Accept", MediaType.APPLICATION_JSON)
				.build(ResourceFixture)
		client = resource.get()
	}

	def cleanup() {
		resource.close()
	}

	def "Content type header handled properly"() {
		given:
			def url = "/api/test/42"
			setupStub(url)

		when:
			def putResponse = client.update(42, "put body")

		then:
			putResponse == "put_response"
			verify(1,
					putRequestedFor(urlEqualTo(url))
					.withRequestBody(equalTo("put body"))
					.withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON))
					.withHeader("Accept", equalTo(MediaType.APPLICATION_JSON))
					)
	}


	private setupStub(String url) {
		wireMockRule.stubFor(
				put(urlEqualTo(url))
				.willReturn(aResponse()
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("put_response"))
				)
	}
}
