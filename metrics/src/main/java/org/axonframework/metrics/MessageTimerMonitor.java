package org.axonframework.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageTimerMonitor implements MessageMonitor<Message<?>>, MetricSet {

    private final Timer all;
    private final Timer successTimer;
    private final Timer failureTimer;

    public MessageTimerMonitor() {
        all = new Timer();
        successTimer = new Timer();
        failureTimer = new Timer();
    }

    @Override
    public MonitorCallback onMessageIngested(Message<?> message) {
        final Timer.Context timerContext = this.all.time();
        final Timer.Context successTimerContext = this.successTimer.time();
        final Timer.Context failureTimerContext = this.failureTimer.time();
        return new MessageMonitor.MonitorCallback() {
            @Override
            public void onSuccess() {
                timerContext.stop();
                successTimerContext.stop();
            }

            @Override
            public void onFailure(Throwable cause) {
                timerContext.stop();
                failureTimerContext.stop();
            }
        };
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("all", all);
        metrics.put("successTimer", successTimer);
        metrics.put("failureTimer", failureTimer);
        return metrics;
    }
}