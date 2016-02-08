package com.adaptris.jaxrscp.reflections

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.client.WebTarget;

import com.adaptris.jaxrscp.WebTargetVisitor;
import com.adaptris.jaxrscp.fixtures.ResourceFixture;

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class WebTargetVisitorSpockTest extends Specification {

	
	WebTargetVisitor visitor
	WebTarget target
	MetaDataReader reader
	
	def setup() {
		target = Mock(WebTarget)
	}
	
	def "It visits target for mixedParams method"() {
		given:		
			reader = (new MetaDataReaderAssert(ResourceFixture, "mixedParams", [String, Integer, String, String, String])).get()
			visitor = new WebTargetVisitor(reader)
		when:
			visitor.visit(target, ["Header 1", 2, "Query 3", "Matrix 4", "Header 5"] as Object[])
		then:
			1 * target.path("/test/mixedParams/{path1}") >> target
			1 * target.resolveTemplate("path1", 2) >> target
			1 * target.queryParam("query1", "Query 3") >> target
			1 * target.matrixParam("matrix1", "Matrix 4") >> target
	}

}
