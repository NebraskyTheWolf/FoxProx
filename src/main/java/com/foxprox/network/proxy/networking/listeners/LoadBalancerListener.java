package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.event.PostLoginEvent;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.core.event.EventHandler;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoadBalancerListener implements Listener {

    private final Map<String, String> IP_LIST = new LinkedHashMap<>();

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {}
}
