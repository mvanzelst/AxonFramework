package org.axonframework.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.axonframework.eventhandling.EventMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class EventProcessorLatencyMonitor implements MessageMonitor<EventMessage<?>>, MetricSet {

    private final AtomicLong lastReceivedTime = new AtomicLong();
    private final AtomicLong lastProcessedTime = new AtomicLong();

    @Override
    public MonitorCallback onMessageIngested(EventMessage<?> message) {
        updateIfMaxValue(lastReceivedTime, message.getTimestamp().toEpochMilli());
        return new MonitorCallback() {
            @Override
            public void onSuccess() {
                updateIfMaxValue(lastProcessedTime, message.getTimestamp().toEpochMilli());
            }

            @Override
            public void onFailure(Optional<Throwable> cause) {
                updateIfMaxValue(lastProcessedTime, message.getTimestamp().toEpochMilli());
            }
        };
    }

    @Override
    public Map<String, Metric> getMetrics() {
        long lastProcessedTimeLocal = this.lastProcessedTime.longValue();
        long lastReceivedTimeLocal = this.lastReceivedTime.longValue();
        long processTime;
        if(lastReceivedTimeLocal == 0 || lastProcessedTimeLocal == 0){
            processTime = 0;
        } else {
            processTime = lastProcessedTimeLocal - lastReceivedTimeLocal;
        }
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("latency", (Gauge<Long>) () -> processTime);
        return metrics;
    }

    private void updateIfMaxValue(AtomicLong atomicLong, long timestamp){
        atomicLong.accumulateAndGet(timestamp, (currentValue, newValue) -> newValue > currentValue ? newValue : currentValue);
    }
}
