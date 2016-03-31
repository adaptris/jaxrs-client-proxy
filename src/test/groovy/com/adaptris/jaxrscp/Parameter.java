package com.adaptris.jaxrscp;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public class Parameter extends HeaderParameter{

	@PathParam("pathParamAV")
	private String pathParam;

	@QueryParam("queryParamAV")
	private String queryParam;
	
	@MatrixParam("matrixParamAV")
	private String matrixParam;
	

}
