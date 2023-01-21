package com.foxprox.network.proxy.lib.event;

import com.foxprox.network.proxy.core.api.plugin.Event;
import com.foxprox.network.proxy.lib.exception.ProxyException;
import com.google.common.base.Preconditions;

/**
 * Called whenever an exception is thrown in a recoverable section of the server.
 */
public class ProxyExceptionEvent extends Event {

    private ProxyException exception;

    public ProxyExceptionEvent(ProxyException exception) {
        this.exception = Preconditions.checkNotNull(exception, "exception");
    }

    /**
     * Gets the wrapped exception that was thrown.
     *
     * @return Exception thrown
     */
    public ProxyException getException() {
        return exception;
    }

}
