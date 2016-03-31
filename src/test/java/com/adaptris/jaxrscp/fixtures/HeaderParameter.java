package com.adaptris.jaxrscp.fixtures;

import javax.ws.rs.HeaderParam;


public abstract class HeaderParameter {

	@HeaderParam("Content-Type")
	private String headerParam;

	@HeaderParam("Header-2")
	private String headerParam2;

	
	
	
	public String getHeaderParam() {
		return headerParam;
	}
	
	public void setHeaderParam(String headerParam) {
		this.headerParam = headerParam;
	}
	
	public String getHeaderParam2() {
		return headerParam2;
	}
	
	public void setHeaderParam2(String headerParam2) {
		this.headerParam2 = headerParam2;
	}
	
	
	
	
}
