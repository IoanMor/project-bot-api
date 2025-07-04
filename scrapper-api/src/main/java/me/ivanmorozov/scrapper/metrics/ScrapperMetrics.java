package me.ivanmorozov.scrapper.metrics;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.common.metric.MetricsRecorder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapperMetrics {
    private final MetricsRecorder metricsRecorder;

    public void recordKafkaMessageCountResponse() {
        metricsRecorder.incrementCounter("kafka.messages.received");
    }
    public void recordKafkaMessageCountResponse(String type) {
        metricsRecorder.incrementCounter("kafka.messages.received", type);
    }

    public void recordApiCallSuccess() {
        metricsRecorder.incrementCounter("scrapper.api.calls.success");
    }

    public void recordApiCallFailure() {
        metricsRecorder.incrementCounter("scrapper.api.calls.failure");
    }
}
