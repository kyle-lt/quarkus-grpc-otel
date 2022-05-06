package com.kjt;

//import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

//import org.jboss.logging.Logger;
import java.util.logging.Logger;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.quarkus.grpc.GlobalInterceptor;

@ApplicationScoped
@GlobalInterceptor
public class OtelServerInterceptor implements ServerInterceptor {

	//private static final Logger logger = Logger.getLogger(OtelServerInterceptor.class);
	private static final Logger logger = Logger.getLogger(OtelServerInterceptor.class.getName());

	// Grab the OTel Resource Attribute values from environment variables
	String otelServiceName = System.getenv("OTEL_SERVICE_NAME") + "_SERVER";
	String otelServiceNamespace = System.getenv("OTEL_SERVICE_NAMESPACE");
	
	// Creating an instance of OtelConfig for this Client Resource
	private OtelConfig otelConfig = new OtelConfig();
	
	/**
	 * The below OpenTelemetry SDK is built with the {@link OtelTracerConfig#OpenTelemetryConfig() OtelTracerConfig} config.
	 * The Resource Attributes for service.name and service.namespace are being provided via environment variables
	 */
	private final OpenTelemetry openTelemetry = otelConfig.OpenTelemetryConfig(otelServiceName, otelServiceNamespace);
	
	private final Tracer tracer = openTelemetry.getTracer("io.opentelemetry.example.HelloWorldServer");

	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {

		logger.info("OtelServerInterceptor called!  Attempting to extract OpenTelemetry W3C Context.");

		Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(),
				headers, OtelUtils.getGetter());

		Span span = tracer.spanBuilder("helloworld.Greeter/SayHello").setParent(extractedContext)
				.setSpanKind(SpanKind.SERVER).startSpan();
		
		try (Scope innerScope = span.makeCurrent()) {
	        span.setAttribute("component", "grpc");
	        span.setAttribute("rpc.service", "Greeter");
	        return next.startCall(call, headers);
		} finally {
			span.end();
		}

	}
}
