package com.kjt;

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

@ApplicationScoped
public class OtelConfig {

	// Logger
	private static final Logger logger = Logger.getLogger(OtelConfig.class);

	/**
	 * Returns an OpenTelemtry SDK bootstrapped, configured, and ready to use. The
	 * caller provides the Resource Attributes as the input parameters.
	 * 
	 * @param otelServiceName      The service.name, provided as a String,
	 *                             originally designed to be provided as an
	 *                             environment variable OTEL_SERVICE_NAME
	 * @param otelServiceNamespace The service.namespace, provided as a String,
	 *                             originally designed to be provided as an
	 *                             environment variable OTEL_SERVICE_NAMESPACE
	 */
	public OpenTelemetry OpenTelemetryConfig(String otelServiceName, String otelServiceNamespace) {

		// ** Create OTLP gRPC Span Exporter & BatchSpanProcessor **
		OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
				.setEndpoint("http://host.docker.internal:4317").setTimeout(2, TimeUnit.SECONDS).build();
		BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
				.setScheduleDelay(100, TimeUnit.MILLISECONDS).build();

		/*
		 * Attempting to try OTEL_RESOURCE_ATTRIBUTES again, let's see what happens (see
		 */
		AttributeKey<String> myServiceName = AttributeKey.stringKey("service.name");
		AttributeKey<String> myServiceNamespace = AttributeKey.stringKey("service.namespace");
		logger.debug("otelServiceName = " + otelServiceName);
		logger.debug("otelServiceNamespace = " + otelServiceNamespace);
		Resource serviceNameResource = Resource
				.create(Attributes.of(myServiceName, otelServiceName, myServiceNamespace, otelServiceNamespace));

		/*
		 *  Create OpenTelemetry SdkTracerProvider
		 *  Use OTLP & Logging Exporters
		 *  Use Service Name Resource (and attributes) defined above
		 *  Use AlwaysOn TraceConfig
		 */
		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder().addSpanProcessor(spanProcessor) // OTLP
				.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
				.setResource(Resource.getDefault().merge(serviceNameResource))
				// .setResource(Resource.getDefault())
				.setSampler(Sampler.alwaysOn()).build();

		// ** Create OpenTelemetry SDK **
		// Use W3C Trace Context Propagation
		// Use the SdkTracerProvider instantiated above
		OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder().setTracerProvider(sdkTracerProvider)
				.setPropagators(ContextPropagators
						.create(W3CTraceContextPropagator.getInstance()))
				//.buildAndRegisterGlobal();
				.build();

		// ** Create Shutdown Hook **
		Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::shutdown));

		return openTelemetrySdk;

	}
}
