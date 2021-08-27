package com.kjt;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@ApplicationScoped
public class OtelServerInterceptor implements ServerInterceptor {
	
	private static final Logger logger = Logger.getLogger(OtelServerInterceptor.class);

	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		
		logger.info("OtelServerInterceptor called!  Attempting to extract OpenTelemetry W3C Context.");
		
		//Metadata.Key<String> otelHeaderTest = Metadata.Key.of("kjt-test-header", Metadata.ASCII_STRING_MARSHALLER);
		logger.info("Here are all of the headers:");
		Set<String> headerKeys = headers.keys();
		for (String headerKeyString : headerKeys) {
			Metadata.Key<String> headerKey = Metadata.Key.of(headerKeyString, Metadata.ASCII_STRING_MARSHALLER);
			logger.info("headerKey = " + headerKeyString);
			logger.info("headerValue = " + headers.get(headerKey));
		}
		
		return next.startCall(call, headers);
	}
}
