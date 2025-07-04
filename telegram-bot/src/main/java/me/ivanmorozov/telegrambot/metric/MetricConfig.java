package me.ivanmorozov.telegrambot.metric;

import io.micrometer.core.instrument.MeterRegistry;
import me.ivanmorozov.common.metric.MetricsRecorder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricConfig {
    @Bean
    public MetricsRecorder metricsRecorder(MeterRegistry meterRegistry){
        return new MetricsRecorder(meterRegistry);
    }
}
