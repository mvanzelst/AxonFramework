package org.axonframework.metrics;

import com.codahale.metrics.Timer;
import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageTimerMonitor implements MessageMonitor<Message<?>> {

    private final Timer timer = new Timer();
    private final Timer successTimer = new Timer();
    private final Timer failureTimer = new Timer();

    @Override
    public MonitorCallback onMessageIngested(Message<?> message) {
        final Timer.Context timerContext = this.timer.time();
        final Timer.Context successTimerContext = this.timer.time();
        final Timer.Context failureTimerContext = this.timer.time();
        return new MessageMonitor.MonitorCallback() {
            @Override
            public void onSuccess() {
                timerContext.stop();
                successTimerContext.stop();
            }

            @Override
            public void onFailure(Exception cause) {
                timerContext.stop();
                failureTimerContext.stop();
            }
        };
    }

    @Override
    public Map<String, Object> getMetricSet() {
        Map<String, Object> metricSet = new HashMap<>();
        metricSet.put("timer", timer);
        metricSet.put("successTimer", successTimer);
        metricSet.put("failureTimer", failureTimer);
        return metricSet;
    }
}