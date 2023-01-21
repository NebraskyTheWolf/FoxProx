package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BedrockLoginEvent extends Event {

    private final UUID player;
}
