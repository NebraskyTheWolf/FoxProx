package com.foxprox.network.proxy.lib.event;

import com.foxprox.network.proxy.core.api.config.ListenerInfo;
import com.foxprox.network.proxy.core.api.plugin.Event;
import com.foxprox.network.proxy.lib.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * This event will be posted whenever a Query request is received.
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
public class ProxyQueryEvent extends Event {
    /**
     * The listener associated with this query.
     */
    @NonNull
    private final ListenerInfo listener;
    /**
     * The query to return.
     */
    @NonNull
    private QueryResult result;
}
