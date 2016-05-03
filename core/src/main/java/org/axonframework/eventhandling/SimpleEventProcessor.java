/*
 * Copyright (c) 2010-2015. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.eventhandling;

import org.axonframework.common.annotation.MessageHandlerInvocationException;
import org.axonframework.messaging.DefaultInterceptorChain;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.CurrentUnitOfWork;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.metrics.MessageMonitor;
import org.axonframework.metrics.NoOpMessageMonitor;

import java.util.List;
import java.util.Set;

/**
 * A simple Event Processor implementation that invokes each of the members of a event processor when an Event is
 * published. When an Event Listener raises an exception, publication of the Event is aborted and the exception is
 * propagated. No guarantees are given about the order of invocation of Event Listeners.
 *
 * @author Allard Buijze
 * @since 1.2
 */
public class SimpleEventProcessor extends AbstractEventProcessor {

    private MessageMonitor<EventMessage<?>> messageMonitor;

    /**
     * Initializes the event processor with given <code>name</code>.
     *
     * @param name The name of this event processor
     */
    public SimpleEventProcessor(String name) {
        super(name);
        this.messageMonitor = new NoOpMessageMonitor();
    }

    public SimpleEventProcessor(String name, MessageMonitor<EventMessage<?>> messageMonitor) {
        super(name);
        this.messageMonitor = messageMonitor;
    }

    public SimpleEventProcessor(String name, MessageMonitor<EventMessage<?>> messageMonitor, EventListener... initialListeners) {
        super(name, initialListeners);
        this.messageMonitor = messageMonitor;
    }

    /**
     * Initializes the event processor with given <code>name</code>, using given <code>orderResolver</code> to define
     * the order in which listeners need to be invoked.
     * <p/>
     * Listeners are invoked with the lowest order first.
     *
     * @param name          The name of this event processor
     * @param orderResolver The resolver defining the order in which listeners need to be invoked
     */
    public SimpleEventProcessor(String name, OrderResolver orderResolver) {
        super(name, new EventListenerOrderComparator(orderResolver));
        this.messageMonitor = new NoOpMessageMonitor();
    }

    public SimpleEventProcessor(String name, EventListener... initialListeners) {
        super(name, initialListeners);
        this.messageMonitor = new NoOpMessageMonitor();
    }

    @Override
    public void doPublish(List<EventMessage<?>> events, Set<EventListener> eventListeners,
                          Set<MessageHandlerInterceptor<EventMessage<?>>> interceptors,
                          MultiplexingEventProcessingMonitor monitor) {
        try {
            for (EventMessage event : events) {
                MessageMonitor.MonitorCallback monitorCallback = messageMonitor.onMessageIngested(event);
                try {
                    UnitOfWork<EventMessage<?>> unitOfWork = DefaultUnitOfWork.startAndGet(event);
                    InterceptorChain<?> interceptorChain = new DefaultInterceptorChain<>(unitOfWork,
                            interceptors, (message, uow) -> {
                        for (EventListener eventListener : eventListeners) {
                            eventListener.handle(message);
                        }
                        return null;
                    });
                    unitOfWork.executeWithResult(interceptorChain::proceed);
                    monitorCallback.onSuccess();
                } catch (Exception e){
                    monitorCallback.onFailure(e);
                    throw e;
                }
            }
            notifyMonitors(events, monitor, null);
        } catch (Exception e) {
            notifyMonitors(events, monitor, e);
            throw new MessageHandlerInvocationException("Error processing published events", e);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void notifyMonitors(List<EventMessage<?>> events, EventProcessingMonitor monitor, Exception exception) {
        if (CurrentUnitOfWork.isStarted()) {
            CurrentUnitOfWork.get().afterCommit(u -> monitor.onEventProcessingCompleted(events));
            CurrentUnitOfWork.get().onRollback(u -> monitor.onEventProcessingFailed(events, exception == null
                    && u.getExecutionResult().isExceptionResult() ? u.getExecutionResult().getExceptionResult()
                    : exception));
        } else if (exception == null) {
            monitor.onEventProcessingCompleted(events);
        } else {
            monitor.onEventProcessingFailed(events, exception);
        }
    }
}
