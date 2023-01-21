package com.foxprox.network.proxy.core.event;

public interface EventExceptionHandler<T> {

    public void handleEventException(String msg, T event, EventHandlerMethod method, Throwable ex);

}
