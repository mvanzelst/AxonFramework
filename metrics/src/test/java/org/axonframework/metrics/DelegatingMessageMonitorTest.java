package org.axonframework.metrics;

import com.codahale.metrics.Metric;
import org.axonframework.messaging.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DelegatingMessageMonitorTest {

    @Test
    public void test_onMessageIngested_SingleMessageMonitor(){
        MessageMonitor<Message<?>> messageMonitorMock = mock(MessageMonitor.class);
        DelegatingMessageMonitor delegatingMessageMonitor = new DelegatingMessageMonitor(Arrays.asList(messageMonitorMock));
        Message messageMock = mock(Message.class);

        delegatingMessageMonitor.onMessageIngested(messageMock);

        verify(messageMonitorMock).onMessageIngested(same(messageMock));
    }

    @Test
    public void test_onMessageIngested_MultipleMessageMonitors(){
        MessageMonitor<Message<?>> messageMonitorMock1 = mock(MessageMonitor.class);
        MessageMonitor<Message<?>> messageMonitorMock2 = mock(MessageMonitor.class);
        DelegatingMessageMonitor delegatingMessageMonitor = new DelegatingMessageMonitor(Arrays.asList(messageMonitorMock1, messageMonitorMock2));
        Message messageMock = mock(Message.class);

        delegatingMessageMonitor.onMessageIngested(messageMock);

        verify(messageMonitorMock1).onMessageIngested(same(messageMock));
        verify(messageMonitorMock2).onMessageIngested(same(messageMock));
    }

    @Test
    public void test_getMetricSet_SingleMessageMonitor(){
        MessageMonitor<Message<?>> messageMonitorMock = mock(MessageMonitor.class);
        Metric metric = mock(Metric.class);
        when(messageMonitorMock.getMetricSet()).thenReturn(Collections.singletonMap("key", metric));

        DelegatingMessageMonitor delegatingMessageMonitor = new DelegatingMessageMonitor(Arrays.asList(messageMonitorMock));
        Map<String, Object> metrics = delegatingMessageMonitor.getMetricSet();
        assertEquals(1, metrics.size());
        assertSame(metric, metrics.get("key"));
    }

    @Test
    public void test_getMetricSet_MultipleMessageMonitor(){
        MessageMonitor<Message<?>> messageMonitorMock1 = mock(MessageMonitor.class);
        Metric metric1 = mock(Metric.class);
        when(messageMonitorMock1.getMetricSet()).thenReturn(Collections.singletonMap("key1", metric1));

        MessageMonitor<Message<?>> messageMonitorMock2 = mock(MessageMonitor.class);
        Metric metric2 = mock(Metric.class);
        when(messageMonitorMock2.getMetricSet()).thenReturn(Collections.singletonMap("key2", metric2));

        DelegatingMessageMonitor delegatingMessageMonitor = new DelegatingMessageMonitor(Arrays.asList(messageMonitorMock1, messageMonitorMock2));
        Map<String, Object> metrics = delegatingMessageMonitor.getMetricSet();
        assertEquals(2, metrics.size());
        assertSame(metric1, metrics.get("key1"));
        assertSame(metric2, metrics.get("key2"));
    }
}