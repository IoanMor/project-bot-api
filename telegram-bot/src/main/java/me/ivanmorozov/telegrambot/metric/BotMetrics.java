package me.ivanmorozov.telegrambot.metric;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.common.metric.MetricsRecorder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotMetrics {
    private final MetricsRecorder metricsRecorder;

    public void recordTelegramMessageCountResponse() {
        metricsRecorder.incrementCounter("telegram.messages.response");
    }

    public void recordTelegramMessageCountResponse(String type) {
        metricsRecorder.incrementCounter("telegram.messages.response",type);
    }

    public void recordKafkaMessageCountRequest() {
        metricsRecorder.incrementCounter("kafka.messages.request");
    }
    public void recordKafkaMessageCountRequest(String type) {
        metricsRecorder.incrementCounter("kafka.messages.request", type);
    }

}
