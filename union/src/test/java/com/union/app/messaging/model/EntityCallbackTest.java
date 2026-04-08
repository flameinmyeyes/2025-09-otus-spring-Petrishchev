package com.union.app.messaging.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityCallbackTest {

    @Test
    void messageAndEventAndPollPopulateTimestampsOnPrePersist() throws Exception {
        Message message = new Message();
        Event event = new Event();
        Poll poll = new Poll();

        invokeOnCreate(message, Message.class);
        invokeOnCreate(event, Event.class);
        invokeOnCreate(poll, Poll.class);

        assertNotNull(message.getTimestamp());
        assertNotNull(event.getCreatedAt());
        assertNotNull(poll.getCreatedAt());
    }

    private static void invokeOnCreate(Object target, Class<?> type) throws Exception {
        Method method = type.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(target);
    }
}
