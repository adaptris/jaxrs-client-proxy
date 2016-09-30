package com.adaptris.jaxrscp

import static com.github.tomakehurst.wiremock.client.WireMock.*

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.PathSegment

import org.junit.Rule

import spock.lang.Specification
import spock.lang.Unroll
import wiremock.org.apache.http.entity.ContentType;

import com.adaptris.jaxrscp.fixtures.CompanyAttributes
import com.adaptris.jaxrscp.fixtures.ResourceFixture
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.junit.WireMockRule

@Unroll
class PathSegmentFixtureSpockIntegrationTest extends Specification{

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
	
	
	def "Path attribute with PathSegment object handled properly"() {
		given:			
			CompanyAttributes sourceCompany = new CompanyAttributes()
			sourceCompany.path = "Company1"
		
			sourceCompany.matrixParameters.add("country","PL")
			sourceCompany.matrixParameters.add("code","ABC")
			sourceCompany.matrixParameters.add("category","Soil_Type")
			def sourceSegment = buildSegment(sourceCompany)
			
			CompanyAttributes targetCompany = new CompanyAttributes()
			targetCompany.path = "Company2"
			targetCompany.matrixParameters.add("category","Soil_Type")
			def targetSegment = buildSegment(targetCompany)
			
			def url =  "/api/test/lookup/$sourceSegment/$targetSegment"
			setupStub(url)

		when:
			def putResponse = client.lookup(sourceCompany, targetCompany)		

		then:
			putResponse == "get_response"
			verify(1,
				getRequestedFor(urlEqualTo(url))
				)
	}

	def "Path attribute encoding with PathSegment object"() {
		given:					
			CompanyAttributes sourceCompany = new CompanyAttributes()
			sourceCompany.path = "Company 1"
		
			sourceCompany.matrixParameters.add("country","PL")
			sourceCompany.matrixParameters.add("code","ABC")
			sourceCompany.matrixParameters.add("category","Soil_Type")
			def sourceSegment = buildSegment(sourceCompany)
			
			CompanyAttributes targetCompany = new CompanyAttributes()
			targetCompany.path = "Company2"
			targetCompany.matrixParameters.add("category","Soil Type")
			def targetSegment = buildSegment(targetCompany)
			
			def url =  "/api/test/lookup/$sourceSegment/$targetSegment"
			setupStub(url)
			

		when:
			def putResponse = client.lookup(sourceCompany, targetCompany);
			

		then:
			putResponse == "get_response"
			verify(1,
					getRequestedFor(urlEqualTo(url))
					)
	}
	
	
	def "Multiple Path attributes PathSegment object"() {
		given:			
			CompanyAttributes sourceCompany = new CompanyAttributes();
			sourceCompany.path = "Company 1"
		
			sourceCompany.matrixParameters.add("country","PL")
			sourceCompany.matrixParameters.add("code","ABC1")
			sourceCompany.matrixParameters.add("code","ABC2")
			sourceCompany.matrixParameters.add("category","Soil_Type")
			
			def sourceSegment = buildSegment(sourceCompany)
			
			CompanyAttributes targetCompany = new CompanyAttributes();
			targetCompany.path = "Company2"
			targetCompany.matrixParameters.add("category","Soil Type")
			
			def targetSegment = buildSegment(targetCompany)
			
			def url =  "/api/test/lookup/$sourceSegment/$targetSegment"
			setupStub(url)
			

		when:
			def putResponse = client.lookup(sourceCompany, targetCompany);
			

		then:
			putResponse == "get_response"
			verify(1,
					getRequestedFor(urlEqualTo(url))
					)
	}
	
	private setupStub(String url) {
		wireMockRule.stubFor(
				get(urlEqualTo(url))
				.willReturn(aResponse()
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("get_response"))
				)
	}
	
	private String buildSegment(PathSegment segment) {
		return PathValueReader.read(segment).replaceAll('\\s', '%20')
	}
}
