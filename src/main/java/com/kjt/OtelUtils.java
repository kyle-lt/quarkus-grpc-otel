package com.kjt;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

/*
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import io.grpc.stub.MetadataUtils;
*/

import io.grpc.Metadata;

import io.opentelemetry.context.propagation.TextMapSetter;

@ApplicationScoped
public class OtelUtils {
	
	private static final Logger logger = Logger.getLogger(OtelUtils.class);
	
	private static final TextMapSetter<Metadata> setter =
		      (carrier, key, value) ->
		          carrier.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);
		          
	public static TextMapSetter<Metadata> getSetter() {
		return setter;
	}
	

		          
		          
	/*
	@Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
		
		logger.info("OtelUtils called!  Attempting to inject OpenTelemetry W3C Context.");
		
		// Create new Metadata headers, apply the interceptor
		
		Metadata.Key<String> otelHeaderTestKey = Metadata.Key.of("kjt-test-header", Metadata.ASCII_STRING_MARSHALLER);
		Metadata otelMetadataTest = new Metadata();
		otelMetadataTest.put(otelHeaderTestKey, "testHeaderVal1234");
		ClientInterceptor headerInterceptor = MetadataUtils.newAttachHeadersInterceptor(otelMetadataTest);
		headerInterceptor.interceptCall(method, callOptions, next);
		
		
		// Return the ClientCall, after headers have been injected
		return next.newCall(method, callOptions);
    }
	*/
	
}
