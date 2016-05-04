package org.axonframework.metrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class CapacityMonitorTest {

    @Test
    public void testSingleThreadedCapacity() {
        TestClock testClock = new TestClock();
        CapacityMonitor testSubject = new CapacityMonitor(1, TimeUnit.SECONDS, testClock);
        MessageMonitor.MonitorCallback monitorCallback = testSubject.onMessageIngested(null);
        testClock.increase(1000);
        monitorCallback.onSuccess();

        Map<String, Metric> metricSet = testSubject.getMetricSet();
        Gauge<Double> capacityGauge = (Gauge<Double>) metricSet.get("capacity");
        assertEquals(1, capacityGauge.getValue(), 0);
    }

    @Test
    public void testMultithreadedCapacity(){
        TestClock testClock = new TestClock();
        CapacityMonitor testSubject = new CapacityMonitor(1, TimeUnit.SECONDS, testClock);
        MessageMonitor.MonitorCallback monitorCallback = testSubject.onMessageIngested(null);
        MessageMonitor.MonitorCallback monitorCallback2 = testSubject.onMessageIngested(null);
        testClock.increase(1000);
        monitorCallback.onSuccess();
        monitorCallback2.onSuccess();

        Map<String, Metric> metricSet = testSubject.getMetricSet();
        Gauge<Double> capacityGauge = (Gauge<Double>) metricSet.get("capacity");
        assertEquals(2, capacityGauge.getValue(), 0);
    }

    @Test
    public void testEmptyCapacity(){
        TestClock testClock = new TestClock();
        CapacityMonitor testSubject = new CapacityMonitor(1, TimeUnit.SECONDS, testClock);
        Map<String, Metric> metricSet = testSubject.getMetricSet();
        Gauge<Double> capacityGauge = (Gauge<Double>) metricSet.get("capacity");
        assertEquals(0, capacityGauge.getValue(), 0);
    }

    private class TestClock extends Clock {

        private long currentTimeInMs = 0;

        @Override
        public long getTick() {
            return currentTimeInMs * 1000;
        }

        @Override
        public long getTime() {
            return currentTimeInMs;
        }

        public void increase(long increaseInMs){
            this.currentTimeInMs += increaseInMs;
        }
    }

}