package org.axonframework.metrics;

public class NoOpMessageMonitorCallback implements MessageMonitor.MonitorCallback {

    @Override
    public void onSuccess() {
    }

    @Override
    public void onFailure(Exception cause) {
    }
}
