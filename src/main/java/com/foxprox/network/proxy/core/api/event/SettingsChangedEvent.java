package com.foxprox.network.proxy.core.api.event;

import com.foxprox.network.proxy.core.api.connection.FoxPlayer;
import com.foxprox.network.proxy.core.api.plugin.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Called after a {@link FoxPlayer} changed one or more of the following
 * (client-side) settings:
 *
 * <ul>
 * <li>View distance</li>
 * <li>Locale</li>
 * <li>Displayed skin parts</li>
 * <li>Chat visibility</li>
 * <li>Chat colors</li>
 * <li>Main hand side (left or right)</li>
 * </ul>
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class SettingsChangedEvent extends Event
{

    /**
     * Player who changed the settings.
     */
    private final FoxPlayer player;
}
