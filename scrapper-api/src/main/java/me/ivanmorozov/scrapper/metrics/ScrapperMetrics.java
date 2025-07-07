package me.ivanmorozov.scrapper.metrics;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.common.metric.MetricsRecorder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ScrapperMetrics {
    private final MetricsRecorder metricsRecorder;


    public void recordKafkaMessageCountResponse(String type) {
        metricsRecorder.incrementCounter("scrapper.messages.response","success", type);
    }

    public void recordApiCallSuccess(String nameApi) {
        metricsRecorder.incrementCounter("scrapper.api.calls.success","success",nameApi);
    }

    public void recordApiCallFailure(String errorType) {
        metricsRecorder.incrementCounter("scrapper.api.calls.failure","error", errorType);
    }
}
