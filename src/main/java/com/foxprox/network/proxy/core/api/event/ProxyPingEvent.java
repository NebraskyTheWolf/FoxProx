package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.connection.PendingConnection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.foxprox.network.proxy.core.api.Callback;
import com.foxprox.network.proxy.core.api.ServerPing;

/**
 * Called when the proxy is pinged with packet 0xFE from the server list.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ProxyPingEvent extends AsyncEvent<ProxyPingEvent>
{

    /**
     * The connection asking for a ping response.
     */
    private final PendingConnection connection;
    /**
     * The data to respond with.
     */
    private ServerPing response;

    public ProxyPingEvent(PendingConnection connection, ServerPing response, Callback<ProxyPingEvent> done)
    {
        super( done );
        this.connection = connection;
        this.response = response;
    }
}
