package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PubSubMessage extends Event {
    private final String pattern;
    private final String channel;
    private final String message;

    public PubSubMessage(String pattern, String channel, String message)
    {
        this.pattern = pattern;
        this.channel = channel;
        this.message = message;
    }
}
