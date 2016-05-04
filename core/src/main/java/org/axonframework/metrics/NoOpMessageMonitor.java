package org.axonframework.metrics;

import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

public enum NoOpMessageMonitor implements MessageMonitor<Message<?>> {

    INSTANCE;

    @Override
    public MonitorCallback onMessageIngested(Message message) {
        return new NoOpMessageMonitorCallback();
    }

    public Map<String, Object> getMetricSet() {
        return new HashMap();
    }
}
