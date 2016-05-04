package org.axonframework.metrics;

import org.axonframework.messaging.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}