package org.axonframework.metrics;

import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

public class NoOpMessageMonitor<T extends Message<?>> implements MessageMonitor<T> {

    @Override
    public MonitorCallback onMessageIngested(Message message) {
        return new NoOpMessageMonitorCallback();
    }

    @Override
    public Map<String, Object> getMetricSet() {
        return new HashMap();
    }
}
