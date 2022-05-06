package com.kjt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;

import io.quarkus.example.GreeterGrpc.GreeterBlockingStub;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.quarkus.example.HelloRequest;
import io.quarkus.grpc.GrpcClient;

@Path("/hello")
public class GreetingResourceOnServer {

	private static final Logger logger = Logger.getLogger(GreetingResourceOnServer.class.getName());
	
	// Grab the OTel Resource Attribute values from environment variables
	String otelServiceName = System.getenv("OTEL_SERVICE_NAME") + "_CLIENT";
	String otelServiceNamespace = System.getenv("OTEL_SERVICE_NAMESPACE");
	
	// Creating an instance of OtelConfig for this Client Resource
	private OtelConfig otelConfig = new OtelConfig();
	
	/**
	 * The below OpenTelemetry SDK is built with the {@link OtelTracerConfig#OpenTelemetryConfig() OtelTracerConfig} config.
	 * The Resource Attributes for service.name and service.namespace are being provided via environment variables
	 */
	public OpenTelemetry openTelemetry = otelConfig.OpenTelemetryConfig(otelServiceName, otelServiceNamespace);
	
	// Instantiate Tracer for this Client Resource
	Tracer tracer = openTelemetry.getTracer("com.ktully.nativeimage.quarkus.restservice");
	
	// Inject the GrpcClient Service that will make gRPC requests to gRPC Server
	@GrpcClient("hello")
	GreeterBlockingStub blockingHelloService;

	/**
	 * GET to /hello simply returns "Hello" and is not traced with OTel
	 * @return String "Hello"
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		
		logger.info("Request at Server's client to /hello");
		return "Hello";
	}
	
	/**
	 * GET to /hello/{name} returns "Hello {name}" and is instrumented with OTel
	 * @param name is pulled from the URI that is requested, e.g., /hello/Kyle, name = Kyle
	 * @return String "Hello/{name}"
	 */
    @GET
    @Path("/{name}")
    public String hello(@PathParam("name") String name) {
    	
    	logger.info("Request at Server's client to /hello/" + name);
    	
    	logger.info("Starting Span now!");
    	Span span = tracer.spanBuilder("helloworld.Greeter/SayHello").setSpanKind(SpanKind.CLIENT).startSpan();    	
    	try (Scope scope = span.makeCurrent()) {
        	// Create an instance of the OtelClientInterceptor
        	OtelClientInterceptor otelClientInterceptor = new OtelClientInterceptor();
        	otelClientInterceptor.setOpenTelemetry(openTelemetry);
        	// Execute the request, using the OtelClientInterceptor to inject the W3C Context
    		return blockingHelloService
        			.withInterceptors(otelClientInterceptor)
        			.sayHello(HelloRequest.newBuilder()
        					.setName(name)
        					.build())
        			.getMessage();
    		
    	} catch (Exception e) {
    		//logger.error("Error during OT section, here it is!", e);
    		logger.info("Error during OT section!");
    		span.setStatus(StatusCode.ERROR, "gRPC status: " + e);
    		return "error";
    	} finally {
    		span.end();
    	}    	
    }
}
