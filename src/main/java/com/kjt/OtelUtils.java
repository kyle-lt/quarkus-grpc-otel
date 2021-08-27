package com.kjt;

import javax.enterprise.context.ApplicationScoped;

import io.grpc.Metadata;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

@ApplicationScoped
public class OtelUtils {

	// Setter
	private static final TextMapSetter<Metadata> setter = (carrier, key, value) -> carrier
			.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);

	public static TextMapSetter<Metadata> getSetter() {
		return setter;
	}

	// Getter
	// Extract the Distributed Context from the gRPC metadata
	private static final TextMapGetter<Metadata> getter = new TextMapGetter<>() {
		@Override
		public Iterable<String> keys(Metadata carrier) {
			return carrier.keys();
		}

		@Override
		public String get(Metadata carrier, String key) {
			Metadata.Key<String> k = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
			if (carrier.containsKey(k)) {
				return carrier.get(k);
			}
			return "";
		}
	};
	
	public static TextMapGetter<Metadata> getGetter() {
		return getter;
	}

}
