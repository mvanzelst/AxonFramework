package org.axonframework.metrics;

import org.axonframework.messaging.Message;

public interface MessageMonitor<T extends Message<?>> {

    MonitorCallback onMessageIngested(T message);

    interface MonitorCallback {

        void onSuccess();

        void onFailure(Throwable cause);
    }
}
