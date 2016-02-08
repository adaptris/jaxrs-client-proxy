package com.adaptris.jaxrscp.fixtures;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public interface GenericResourceFixture<ID, T> {

	@POST
	public T create(T data);
	
	@PUT
	@Path("/{id}")
	public T update(@PathParam("id") ID id, T data);
	
	@Path("/{id}")
	@DELETE
	public void remove(@PathParam("id") ID id);

	@GET
	@Path("/{id}")
	public T get(@PathParam("id") ID id);
	
}
