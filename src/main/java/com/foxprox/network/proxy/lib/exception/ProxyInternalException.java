package com.foxprox.network.proxy.lib.exception;

import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.lib.event.ProxyExceptionEvent;

/**
 * Thrown when the internal server throws a recoverable exception.
 */
public class ProxyInternalException extends ProxyException {

    public ProxyInternalException(String message) {
        super(message);
    }

    public ProxyInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyInternalException(Throwable cause) {
        super(cause);
    }

    protected ProxyInternalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static void reportInternalException(Throwable cause) {
        try {
            FoxServer.getInstance().getPluginManager().callEvent(new ProxyExceptionEvent(new ProxyInternalException(cause)));;
        } catch (Throwable t) {
            t.printStackTrace(); // Don't want to rethrow!
        }
    }
}
