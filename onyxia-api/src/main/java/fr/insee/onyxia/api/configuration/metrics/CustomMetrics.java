package fr.insee.onyxia.api.configuration.metrics;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@Service
public class CustomMetrics implements MeterBinder {
	
	private static Counter counter;
	
	public void plusUn() {
		counter.increment();
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		counter = Counter.builder("servicespopes")
        .description("Nombre de services popes")
        .tags("services","nombre")
        .baseUnit("services")
        .register(registry);
	}

}
