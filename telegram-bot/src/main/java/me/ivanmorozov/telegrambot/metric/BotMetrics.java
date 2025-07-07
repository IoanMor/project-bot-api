package me.ivanmorozov.telegrambot.metric;

import lombok.RequiredArgsConstructor;
import me.ivanmorozov.common.metric.MetricsRecorder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotMetrics {
    private final MetricsRecorder metricsRecorder;


    public void recordTelegramMessageCountResponse(String type) {
        metricsRecorder.incrementCounter("telegram.messages.response","name",type);
    }

    public void recordKafkaMessageCountRequest(String type) {
        metricsRecorder.incrementCounter("telegram.messages.request","name",type);
    }

}
