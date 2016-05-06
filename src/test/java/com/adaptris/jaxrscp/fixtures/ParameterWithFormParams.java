package com.adaptris.jaxrscp.fixtures;

import javax.ws.rs.FormParam;
import javax.ws.rs.MatrixParam;

public class ParameterWithFormParams extends HeaderParameter{

	@FormParam("paramA")
	private String formParamA;

	@FormParam("paramB")
	private String formParamB;
	
	@FormParam("paramC")
	private String formParamC;

	public String getFormParamA() {
		return formParamA;
	}

	public void setFormParamA(String formParamA) {
		this.formParamA = formParamA;
	}

	public String getFormParamB() {
		return formParamB;
	}

	public void setFormParamB(String formParamB) {
		this.formParamB = formParamB;
	}

	public String getFormParamC() {
		return formParamC;
	}

	public void setFormParamC(String formParamC) {
		this.formParamC = formParamC;
	}
	

}
