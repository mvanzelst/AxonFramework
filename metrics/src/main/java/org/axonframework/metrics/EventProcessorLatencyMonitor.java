package org.axonframework.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.axonframework.eventhandling.EventMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class EventProcessorLatencyMonitor implements MessageMonitor<EventMessage<?>> {

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
            public void onFailure(Throwable cause) {
                updateIfMaxValue(lastProcessedTime, message.getTimestamp().toEpochMilli());
            }
        };
    }

    public Map<String, Metric> getMetricSet() {
        long lastProcessedTimeLocal = this.lastProcessedTime.longValue();
        long lastReceivedTimeLocal = this.lastReceivedTime.longValue();
        long processTime;
        if(lastReceivedTimeLocal == 0 || lastProcessedTimeLocal == 0){
            processTime = 0;
        } else {
            processTime = lastProcessedTimeLocal - lastReceivedTimeLocal;
        }
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("processLatency", (Gauge)() -> processTime);
        metrics.put("processLatency", (Gauge<Long>) () -> processTime);
        return metrics;
    }

    private void updateIfMaxValue(AtomicLong atomicLong, long timestamp){
        atomicLong.accumulateAndGet(timestamp, (currentValue, newValue) -> newValue > currentValue ? newValue : currentValue);
    }
}
