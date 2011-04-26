/*
 * Copyright (c) 2010-2011. Axon Framework
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

package org.axonframework.test.matchers;

import org.axonframework.domain.Event;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;

import java.util.Arrays;

import static org.axonframework.test.matchers.EventMatchers.listWithAllOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Allard Buijze
 */
public class ListWithAllOfMatcherTest {

    private Matcher<Event> mockMatcher1;
    private Matcher<Event> mockMatcher2;
    private Matcher<Event> mockMatcher3;
    private ListWithAllOfMatcher testSubject;
    private StubEvent stubEvent1;
    private StubEvent stubEvent2;

    @SuppressWarnings({"unchecked"})
    @Before
    public void setUp() {
        mockMatcher1 = mock(Matcher.class);
        mockMatcher2 = mock(Matcher.class);
        mockMatcher3 = mock(Matcher.class);
        testSubject = listWithAllOf(mockMatcher1, mockMatcher2, mockMatcher3);
        stubEvent1 = new StubEvent();
        stubEvent2 = new StubEvent();
        when(mockMatcher1.matches(any())).thenReturn(true);
        when(mockMatcher2.matches(any())).thenReturn(true);
        when(mockMatcher3.matches(any())).thenReturn(true);
    }

    @Test
    public void testMatch_FullMatch() {
        assertTrue(testSubject.matchesEventList(Arrays.asList(stubEvent1, stubEvent2)));

        verify(mockMatcher1).matches(stubEvent1);
        verify(mockMatcher1).matches(stubEvent2);
        verify(mockMatcher2).matches(stubEvent1);
        verify(mockMatcher2).matches(stubEvent2);
        verify(mockMatcher3).matches(stubEvent1);
        verify(mockMatcher3).matches(stubEvent2);
    }

    @Test
    public void testMatch_OnlyOneEventMatches() {
        when(mockMatcher1.matches(stubEvent1)).thenReturn(false);
        when(mockMatcher2.matches(stubEvent1)).thenReturn(false);
        when(mockMatcher3.matches(stubEvent1)).thenReturn(false);

        assertTrue(testSubject.matchesEventList(Arrays.asList(stubEvent1, stubEvent2)));

        verify(mockMatcher1).matches(stubEvent1);
        verify(mockMatcher1).matches(stubEvent2);
        verify(mockMatcher2).matches(stubEvent1);
        verify(mockMatcher2).matches(stubEvent2);
        verify(mockMatcher3).matches(stubEvent1);
        verify(mockMatcher3).matches(stubEvent2);
    }

    @Test
    public void testMatch_OneMatcherDoesNotMatch() {
        when(mockMatcher1.matches(any())).thenReturn(false);
        when(mockMatcher2.matches(stubEvent1)).thenReturn(false);
        when(mockMatcher3.matches(stubEvent1)).thenReturn(false);

        assertFalse(testSubject.matchesEventList(Arrays.asList(stubEvent1, stubEvent2)));

        verify(mockMatcher1).matches(stubEvent1);
        verify(mockMatcher1).matches(stubEvent2);
    }

    @Test
    public void testDescribe() {
        testSubject.matchesEventList(Arrays.asList(stubEvent1, stubEvent2));

        doAnswer(new DescribingAnswer("A")).when(mockMatcher1).describeTo(isA(Description.class));
        doAnswer(new DescribingAnswer("B")).when(mockMatcher2).describeTo(isA(Description.class));
        doAnswer(new DescribingAnswer("C")).when(mockMatcher3).describeTo(isA(Description.class));
        StringDescription description = new StringDescription();
        testSubject.describeTo(description);
        String actual = description.toString();
        assertEquals("list with all of: A, B and C", actual);
    }

    @Test
    public void testDescribe_OneMatcherFailed() {
        when(mockMatcher2.matches(any())).thenReturn(false);

        testSubject.matchesEventList(Arrays.asList(stubEvent1, stubEvent2));

        doAnswer(new DescribingAnswer("A")).when(mockMatcher1).describeTo(isA(Description.class));
        doAnswer(new DescribingAnswer("B")).when(mockMatcher2).describeTo(isA(Description.class));
        doAnswer(new DescribingAnswer("C")).when(mockMatcher3).describeTo(isA(Description.class));
        StringDescription description = new StringDescription();
        testSubject.describeTo(description);
        String actual = description.toString();
        assertEquals("list with all of: A, B (FAILED!) and C", actual);
    }

    private static class DescribingAnswer implements Answer<Object> {
        private String description;

        public DescribingAnswer(String description) {
            this.description = description;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Description descriptionParameter = (Description) invocation.getArguments()[0];
            descriptionParameter.appendText(this.description);
            return Void.class;
        }
    }
}
