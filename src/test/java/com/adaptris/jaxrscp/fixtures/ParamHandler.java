package com.adaptris.jaxrscp.fixtures;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public class ParamHandler {

	@HeaderParam("Content-Type")
	private String headerParam;

	@PathParam("pathParamAV")
	private String pathParam;

	@QueryParam("queryParamAV")
	private String queryParam;
	
	@MatrixParam("matrixParamAV")
	private String matrixParam;
	
	@HeaderParam("Header-2")
	private String headerParam2;

}
