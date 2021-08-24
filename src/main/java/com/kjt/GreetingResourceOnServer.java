package com.kjt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.example.GreeterGrpc.GreeterBlockingStub;
import io.quarkus.example.HelloRequest;
import io.quarkus.grpc.GrpcClient;

@Path("/hello")
public class GreetingResourceOnServer {
	
	private static final Logger logger = Logger.getLogger(GreetingResourceOnServer.class);
	
	@GrpcClient("hello")
	GreeterBlockingStub blockingHelloService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		
		logger.info("Request at Server's client to /hello");
		return "Hello";
	}
	
    @GET
    @Path("/{name}")
    public String hello(@PathParam("name") String name) {
    	
    	logger.info("Request at Server's client to /hello/" + name);
    	return blockingHelloService.sayHello(HelloRequest.newBuilder().setName(name).build()).getMessage();
    }

}
