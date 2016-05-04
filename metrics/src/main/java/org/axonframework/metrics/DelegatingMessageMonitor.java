package org.axonframework.metrics;

import org.axonframework.common.Assert;
import org.axonframework.messaging.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DelegatingMessageMonitor<T extends Message<?>> implements MessageMonitor<T> {

    private final List<MessageMonitor<? super T>> messageMonitors;

    public DelegatingMessageMonitor(List<MessageMonitor<? super T>> messageMonitors) {
        Assert.notNull(messageMonitors, "MessageMonitor list may not be null");
        this.messageMonitors = new ArrayList<>(messageMonitors);
    }

    @Override
    public MonitorCallback onMessageIngested(T message) {
        List<MonitorCallback> monitorCallbacks = messageMonitors.stream()
                .map(eventBusMonitor -> eventBusMonitor.onMessageIngested(message))
                .collect(Collectors.toList());

        return new MonitorCallback() {
            @Override
            public void onSuccess() {
                monitorCallbacks.forEach(MonitorCallback::onSuccess);
            }
            @Override
            public void onFailure(Throwable cause) {
                monitorCallbacks.forEach(resultCallback -> resultCallback.onFailure(cause));
            }
        };
    }

}