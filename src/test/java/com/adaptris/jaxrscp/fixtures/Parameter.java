package com.adaptris.jaxrscp.fixtures;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.adaptris.jaxrscp.HeaderParameter;

public class Parameter extends HeaderParameter{

	@PathParam("pathParamAV")
	private String pathParam;

	@QueryParam("queryParamAV")
	private String queryParam;
	
	@MatrixParam("matrixParamAV")
	private String matrixParam;
	

}
