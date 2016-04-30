package org.axonframework.metrics;

public interface EventBusMonitor extends Monitor {

    ResultCallBack onEventPublished();

}
