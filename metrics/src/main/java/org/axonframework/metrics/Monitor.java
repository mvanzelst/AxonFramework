package org.axonframework.metrics;

public interface Monitor {

    <T extends Number> MetricSupplier<T> registerCustomMetric(String metricName, Class<T> metricType);
}
