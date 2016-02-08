jaxrs-client-proxy
======
The aim of the project is to provide and easy way to run your jax-rs annotated resource interfaces as an executable clients

#### Usage

Let's assume you have jax-rs annotated interface similar to this:
```java
	
	package com.yourcompany.api.public;
	
	import javax.ws.rs.*; //Import of all needed jax-rs annotations
	import com.yourcomapny.api.public.model.ResourceData;
	
	@Produces("application/json")
	@Consumes({"application/json", "text/xml"})
	@Path("/resource")
	public interface Resource {
	
		@GET
		@Path("/{id}")
		public ResourceData getById(@PathParam("id") Integer id);
	}
```

Now you would like to connect to resource server hosting this interface. Using this library you just need to make a proxy on the interface and then directly call the methods.

```java

	import com.adaptris.jaxrscp.Resource;
	import com.adaptris.jaxrscp.ResourceBuilder;
	
	...
	
	ResourceBuilder builder = new ResourceBuilder();
	resource = builder.
		url("http://url:port/api").
		bearerAccessToken("fake-access-token").
		build(Resource.class);
	client = resource.get();
	ResourceData data = client.getById(42); //Here is the api call made - GET http://url:port/api/resource/42
	//Then you can make other calls on client
	
	resource.close(); //Remember to close resource after usage (as required by jax-rs client specification)
		
	...

```

#### Dependencies
- java 1.7
- google-guava
- jax-rs

#### What is not supported
- `@CookieParam` annotations and no plans for supporting
- SubResources, as for now this library only support interfaces
- `@FormParam` - not supported yet
- `@BeanParam` - not supported yet

#### What next?
- caching of reflection data reading
- `@FormParam` support
- `@BeanParam` support
