package fr.insee.onyxia.api.configuration.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Service;

@Service
public class CustomMetrics implements MeterBinder {

    private Counter counter;

    public void plusUn() {
        counter.increment();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        counter =
                Counter.builder("servicespopes")
                        .description("Nombre de services popes")
                        .tags("services", "nombre")
                        .baseUnit("services")
                        .register(registry);
    }
}
