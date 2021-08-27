package com.kjt;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.opentelemetry.context.Context;
import io.opentelemetry.api.OpenTelemetry;

public class OtelClientInterceptor implements ClientInterceptor {
	
	private static OpenTelemetry openTelemetry;
	
	public void setOpenTelemetry(OpenTelemetry openTelemetry) {
		OtelClientInterceptor.openTelemetry = openTelemetry;
	}
	
	public OpenTelemetry getOpenTelemetry() {
		return OtelClientInterceptor.openTelemetry;
	}

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
			CallOptions callOptions, Channel next) {
		return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				// Inject the request with the current context
				openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers,
						OtelUtils.getSetter());
				// Perform the gRPC request
				super.start(responseListener, headers);
			}
		};
	}

}
