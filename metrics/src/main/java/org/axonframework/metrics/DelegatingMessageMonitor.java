package org.axonframework.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.axonframework.messaging.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DelegatingMessageMonitor implements MessageMonitor<Message<?>> {

    private final List<MessageMonitor<Message<?>>> messageMonitors;

    public DelegatingMessageMonitor(MessageMonitor<Message<?>>... messageMonitors) {
        this.messageMonitors = Arrays.asList(messageMonitors);
    }

    @Override
    public MonitorCallback onMessageIngested(Message<?> message) {
        List<MonitorCallback> monitorCallbacks = messageMonitors.stream()
                .map(eventBusMonitor -> eventBusMonitor.onMessageIngested(message))
                .collect(Collectors.toList());

        return new MonitorCallback() {
            @Override
            public void onSuccess() {
                monitorCallbacks.forEach(MonitorCallback::onSuccess);
            }
            @Override
            public void onFailure(Exception cause) {
                monitorCallbacks.forEach(resultCallback -> resultCallback.onFailure(cause));
            }
        };
    }

    @Override
    public MetricSet getMetricSet() {
        Map<String, Metric> metrics = new HashMap<>();
        messageMonitors.forEach(eventBusMonitor -> metrics.putAll(eventBusMonitor.getMetricSet().getMetrics()));
        return () -> metrics;
    }
}