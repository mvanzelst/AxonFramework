package org.axonframework.metrics;

import com.codahale.metrics.*;
import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CapacityMonitor implements MessageMonitor<Message<?>>, MetricSet {

    private final Histogram processedDurationHistogram;
    private final TimeUnit timeUnit;
    private final long window;
    private final Clock clock;
    private final Metric ratio;

    public CapacityMonitor(long window, TimeUnit timeUnit) {
        this(window, timeUnit, Clock.defaultClock());
    }

    public CapacityMonitor(long window, TimeUnit timeUnit, Clock clock) {
        SlidingTimeWindowReservoir slidingTimeWindowReservoir = new SlidingTimeWindowReservoir(window, timeUnit, clock);
        this.processedDurationHistogram = new Histogram(slidingTimeWindowReservoir);
        this.timeUnit = timeUnit;
        this.window = window;
        this.clock = clock;
        this.ratio = new RatioGauge();
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

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("ratio", ratio);
        return metrics;
    }

    private class RatioGauge implements Gauge<Double> {
        @Override
        public Double getValue() {
            Snapshot snapshot = processedDurationHistogram.getSnapshot();
            double meanProcessTime = snapshot.getMean();
            int numProcessed = snapshot.getValues().length;
            return  (numProcessed * meanProcessTime) / timeUnit.toMillis(window);
        }
    }
}