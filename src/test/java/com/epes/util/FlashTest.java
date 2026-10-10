package com.epes.util;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Flash and FlashMessage unit tests")
class FlashTest {

    private HttpSession createFakeSession() {
        Map<String, Object> attributes = new HashMap<>();

        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("getAttribute".equals(name)) {
                            return attributes.get(args[0]);
                        } else if ("setAttribute".equals(name)) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        } else if ("removeAttribute".equals(name)) {
                            return attributes.remove(args[0]);
                        }
                        return null;
                    }
                }
        );
    }

    @Test
    @DisplayName("add and consume returns messages once, then empty on subsequent call")
    void testFlashAddAndConsume() {
        HttpSession session = createFakeSession();

        // Initially empty
        assertTrue(Flash.consume(session).isEmpty());
        assertTrue(Flash.consume(null).isEmpty());

        // Add two messages
        Flash.add(session, FlashMessage.Type.SUCCESS, "Record saved successfully");
        Flash.add(session, FlashMessage.Type.ERROR, "An error occurred");

        // First consume returns both
        List<FlashMessage> messages = Flash.consume(session);
        assertEquals(2, messages.size());

        assertEquals(FlashMessage.Type.SUCCESS, messages.get(0).getType());
        assertEquals("Record saved successfully", messages.get(0).getText());
        assertEquals("success", messages.get(0).getBootstrapClass());

        assertEquals(FlashMessage.Type.ERROR, messages.get(1).getType());
        assertEquals("An error occurred", messages.get(1).getText());
        assertEquals("danger", messages.get(1).getBootstrapClass());

        // Second consume returns empty list
        assertTrue(Flash.consume(session).isEmpty());
    }
}
