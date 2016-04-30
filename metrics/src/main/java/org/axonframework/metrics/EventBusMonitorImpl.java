package org.axonframework.metrics;

public class EventBusMonitorImpl implements EventBusMonitor {
    @Override
    public ResultCallBack onEventPublished() {
        long start = System.currentTimeMillis();
        return new ResultCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String reason) {

            }
        };
    }

    @Override
    public <T extends Number> MetricSupplier<T> registerCustomMetric(String metricName, Class<T> metricType) {
        return new
    }
}
