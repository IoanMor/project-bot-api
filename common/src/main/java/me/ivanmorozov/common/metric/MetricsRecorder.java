package me.ivanmorozov.common.metric;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsRecorder {
    private final MeterRegistry meterRegistry;

    public void incrementCounter(String nameMetric){
        meterRegistry.counter(nameMetric).increment();
    }
    public void incrementCounter(String nameMetric, String type){
        meterRegistry.counter(nameMetric,type).increment();
    }
}
