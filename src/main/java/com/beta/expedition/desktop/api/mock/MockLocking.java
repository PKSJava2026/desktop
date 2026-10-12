package com.beta.expedition.desktop.api.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

final class MockLocking {

    private MockLocking() {
    }

    static <T> T synchronize(Class<T> type, T target, Object lock) {
        Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (instance, method, args) -> {
                    synchronized (lock) {
                        try {
                            return method.invoke(target, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
                });
        return type.cast(proxy);
    }
}
