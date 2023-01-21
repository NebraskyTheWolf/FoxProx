package com.foxprox.network.proxy.core.api.permissions;

import net.samagames.persistanceapi.beans.permissions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityPermissions {
    public final UUID Player;

    protected final APIPermissionsBean apiPermissionsBean;
    protected final BukkitPermissionsBean bukkitPermissionsBean;
    protected final BungeeRedisPermissionsBean bungeeRedisPermissionsBean;
    protected final ModerationPermissionsBean moderationPermissionsBean;
    protected final ProxiesPermissionsBean proxiesPermissionsBean;
    protected final StaffPermissionsBean staffPermissionsBean;

    public EntityPermissions(UUID Player,
                             APIPermissionsBean apiPermissionsBean,
                             BukkitPermissionsBean bukkitPermissionsBean,
                             BungeeRedisPermissionsBean bungeeRedisPermissionsBean,
                             ModerationPermissionsBean moderationPermissionsBean,
                             ProxiesPermissionsBean proxiesPermissionsBean,
                             StaffPermissionsBean staffPermissionsBean) {
        this.Player = Player;
        this.apiPermissionsBean = apiPermissionsBean;
        this.bukkitPermissionsBean = bukkitPermissionsBean;
        this.bungeeRedisPermissionsBean = bungeeRedisPermissionsBean;
        this.moderationPermissionsBean = moderationPermissionsBean;
        this.proxiesPermissionsBean = proxiesPermissionsBean;
        this.staffPermissionsBean = staffPermissionsBean;
    }

    public UUID getPlayer() {
        return Player;
    }

    public APIPermissionsBean getApiPermissionsBean() {
        return apiPermissionsBean;
    }

    public BungeeRedisPermissionsBean getBungeeRedisPermissionsBean() {
        return bungeeRedisPermissionsBean;
    }

    public ModerationPermissionsBean getModerationPermissionsBean() {
        return moderationPermissionsBean;
    }

    public ProxiesPermissionsBean getProxiesPermissionsBean() {
        return proxiesPermissionsBean;
    }

    public StaffPermissionsBean getStaffPermissionsBean() {
        return staffPermissionsBean;
    }

    public Map<String, Boolean> getListPermissions() {
        Map<String, Boolean> PERMISSIONS = new LinkedHashMap<>();
        PERMISSIONS.putAll(this.apiPermissionsBean.getHashMap());
        PERMISSIONS.putAll(this.bukkitPermissionsBean.getHashMap());
        PERMISSIONS.putAll(this.bungeeRedisPermissionsBean.getHashMap());
        PERMISSIONS.putAll(this.moderationPermissionsBean.getHashMap());
        PERMISSIONS.putAll(this.proxiesPermissionsBean.getHashMap());
        PERMISSIONS.putAll(this.staffPermissionsBean.getHashMap());
        return PERMISSIONS;
    }
}
