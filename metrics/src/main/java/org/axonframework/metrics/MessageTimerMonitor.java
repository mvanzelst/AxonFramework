package org.axonframework.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import org.axonframework.messaging.Message;

import java.util.Map;

public class MessageTimerMonitor implements MessageMonitor<Message<?>>, MetricSet {

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
            public void onFailure(Throwable cause) {
                timerContext.stop();
                failureTimerContext.stop();
            }
        };
    }

    @Override
    public Map<String, Metric> getMetrics() {
        MetricRegistry mr = new MetricRegistry();
        mr.register("all", timer);
        mr.register("success", successTimer);
        mr.register("failure", failureTimer);
        return mr.getMetrics();
        metricSet.put("timer", timer);
        metricSet.put("successTimer", successTimer);
        metricSet.put("failureTimer", failureTimer);
        return metricSet;
    }
}