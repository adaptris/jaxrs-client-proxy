package com.adaptris.jaxrscp.fixtures;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

public class CompanyAttributes implements PathSegment{
	
	String path;
	MultivaluedMap<String, String> matrixParameters = new MultivaluedHashMap<String, String>(); 
	

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public MultivaluedMap<String, String> getMatrixParameters() {
		return matrixParameters;
	}

	
	
}
