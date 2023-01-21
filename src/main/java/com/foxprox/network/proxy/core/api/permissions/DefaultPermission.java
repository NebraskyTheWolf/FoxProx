package com.foxprox.network.proxy.core.api.permissions;

import net.samagames.persistanceapi.beans.permissions.*;
import java.util.UUID;

public class DefaultPermission extends EntityPermissions{
    public DefaultPermission(UUID Player) {
        super(Player,
                new APIPermissionsBean(
                        0L,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false),
                new BukkitPermissionsBean(
                        0L,
                        false,
                        false,
                        false,
                        false,
                        false),
                new BungeeRedisPermissionsBean(
                        0L,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                ),
                new ModerationPermissionsBean(
                        0L,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                ),
                new ProxiesPermissionsBean(
                        0L,
                        false,
                        false,
                        false,
                        false,
                        false
                ),
                new StaffPermissionsBean(
                        0L,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                ));
    }
}
