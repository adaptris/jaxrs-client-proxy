package com.adaptris.jaxrscp.fixtures;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.adaptris.jaxrscp.Parameter;

@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json", "text/xml"})
@Path("/test")
public interface ResourceFixture extends GenericResourceFixture<Integer, String> {

	
	@POST
	@Path("/xml")
	@Produces("text/xml")
	@Consumes("text/xml")
	public void xmlReader();
	

	@Path("/defaultMethodShouldBeGET")
	public void defaultMethodShouldBeGET();
	
	@GET
	@Path("/header")
	public void header(@HeaderParam("Test-Header") String test);
	
	@GET
	@Path("/mixedParams/{path1}")
	public void mixedParams(
		@HeaderParam("Header-1") String header1,
		@PathParam("path1") Integer path1,
		@QueryParam("query1") String query1,
		@MatrixParam("matrix1") String matrix1,		
		@HeaderParam("Header-2") String header2
	);
	
	@POST
	@Path("/mixedFormParams/{path1}")
	public void mixedFormParams(
		@FormParam("form1") String form1,
		@PathParam("path1") Integer path1,
		@FormParam("form2") String form2
	);
	
	
	@POST
	@Path("/beanParam")
	public void beanParam(
			@BeanParam Parameter paramHandler,
			@HeaderParam("Header-2") String header2
	);
}
