package org.axonframework.metrics;

import com.codahale.metrics.*;
import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CapacityMonitor implements MessageMonitor<Message<?>> {

    private final Histogram processedDurationHistogram;
    private final TimeUnit timeUnit;
    private final long window;
    private final Clock clock;

    public CapacityMonitor(long window, TimeUnit timeUnit) {
        this(window, timeUnit, Clock.defaultClock());
    }

    public CapacityMonitor(long window, TimeUnit timeUnit, Clock clock) {
        SlidingTimeWindowReservoir slidingTimeWindowReservoir = new SlidingTimeWindowReservoir(window, timeUnit, clock);
        this.processedDurationHistogram = new Histogram(slidingTimeWindowReservoir);
        this.timeUnit = timeUnit;
        this.window = window;
        this.clock = clock;
    }

    @Override
    public MonitorCallback onMessageIngested(Message<?> message) {
        final long start = clock.getTime();
        return new MonitorCallback() {
            @Override
            public void onSuccess() {
                processedDurationHistogram.update(clock.getTime() - start);
            }

            @Override
            public void onFailure(Throwable cause) {
                processedDurationHistogram.update(clock.getTime() - start);
            }
        };
    }

    public Map<String, Metric> getMetricSet() {
        Snapshot snapshot = processedDurationHistogram.getSnapshot();
        double meanProcessTime = snapshot.getMean();
        int numProcessed = snapshot.getValues().length;
        double capacity = (numProcessed * meanProcessTime) / timeUnit.toMillis(window);
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("capacity", (Gauge)() -> capacity);
        metrics.put("capacity", (Gauge<Double>) () -> capacity);
        return metrics;
    }
}