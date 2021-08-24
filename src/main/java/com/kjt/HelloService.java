package com.kjt;

import org.jboss.logging.Logger;

import io.quarkus.example.Greeter;
import io.quarkus.example.HelloReply;
import io.quarkus.example.HelloRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService 
public class HelloService implements Greeter {
	
	// Logger
	private static final Logger logger = Logger.getLogger(HelloService.class);

    @Override
    public Uni<HelloReply> sayHello(HelloRequest request) {
    	
    	logger.info("Request received at Server!");
    	
        return Uni.createFrom().item(() ->
                HelloReply.newBuilder().setMessage("Hello " + request.getName()).build()
        );
    }
}
