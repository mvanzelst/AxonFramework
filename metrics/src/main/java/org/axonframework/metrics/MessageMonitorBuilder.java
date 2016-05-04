package org.axonframework.metrics;

import com.codahale.metrics.MetricRegistry;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventhandling.EventMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageMonitorBuilder {

    public MessageMonitor<EventMessage<?>> buildEventMessageMonitor(MetricRegistry globalRegistry){
        MetricRegistry mr = new MetricRegistry();
        globalRegistry.register("eventProcessing", mr);
        List<MessageMonitor<? super EventMessage<?>>> monitors = new ArrayList<>();
        MessageTimerMonitor e = new MessageTimerMonitor();
        mr.register("timers", e);
        monitors.add(e);
        monitors.add(new CapacityMonitor(10, TimeUnit.MINUTES));
        monitors.add(new EventProcessorLatencyMonitor());
        monitors.add(new MessageCountingMonitor());
        return new DelegatingMessageMonitor<>(monitors);
    }

    public MessageMonitor<CommandMessage<?>> buildCommandMessageMonitor(MetricRegistry metricRegistry){
        List<MessageMonitor<? super CommandMessage<?>>> monitors = new ArrayList<>();
        monitors.add(new MessageTimerMonitor());
        monitors.add(new CapacityMonitor(10, TimeUnit.MINUTES));
        monitors.add(new MessageCountingMonitor());
        return new DelegatingMessageMonitor<>(monitors);
    }
}
