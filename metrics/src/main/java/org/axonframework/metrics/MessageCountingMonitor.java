package org.axonframework.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageCountingMonitor implements MessageMonitor<Message<?>> {

    private final Counter ingestedCounter = new Counter();
    private final Counter successCounter = new Counter();
    private final Counter failureCounter = new Counter();
    private final Counter processedCounter = new Counter();

    @Override
    public MonitorCallback onMessageIngested(Message<?> message) {
        ingestedCounter.inc();
        return new MessageMonitor.MonitorCallback() {
            @Override
            public void onSuccess() {
                processedCounter.inc();
                successCounter.inc();
            }

            @Override
            public void onFailure(Exception cause) {
                processedCounter.inc();
                failureCounter.inc();
            }
        };
    }

    @Override
    public MetricSet getMetricSet() {
        Map<String, Metric> metricSet = new HashMap<>();
        metricSet.put("ingestedCounter", ingestedCounter);
        metricSet.put("processedCounter", processedCounter);
        metricSet.put("successCounter", successCounter);
        metricSet.put("failureCounter", failureCounter);
        return () -> metricSet;
    }


}