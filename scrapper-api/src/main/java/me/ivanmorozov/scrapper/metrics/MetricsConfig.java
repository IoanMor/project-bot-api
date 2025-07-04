package me.ivanmorozov.scrapper.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import me.ivanmorozov.common.metric.MetricsRecorder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsConfig {
    @Bean
    public MetricsRecorder metricsRecorder (MeterRegistry meterRegistry){
        return new MetricsRecorder(meterRegistry);
    }

}
