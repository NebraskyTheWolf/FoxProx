package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.config.ServerInfo;
import com.foxprox.network.proxy.core.api.event.PubSubMessage;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.samagames.api.channels.JsonModMessage;
import com.foxprox.network.proxy.samagames.api.channels.ModChannel;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.event.EventHandler;

import java.net.InetSocketAddress;
import java.util.Map;

public class ServersListeners implements Listener {
    @EventHandler
    public void onPubSub(PubSubMessage message) {
        if (message.getChannel().equals("servers")) {
            String[] arguments = message.getMessage().split(" ");

            Map<String, ServerInfo> servers = FoxServer.getInstance().getServers();

            switch (arguments[0]) {
                case "heartbeat": {
                    String name = arguments[1];
                    String ip   = arguments[2];
                    String port = arguments[3];

                    if (!servers.containsKey(name)) {
                        servers.put(name, FoxServer.getInstance().constructServerInfo(name, new InetSocketAddress(ip, Integer.parseInt(port)), "Please connect with mc.floofflight.net.", false));
                        FoxServer.getInstance().getLogger().info(String.format("Adding %s to the server list. with IP: %s, PORT: %s", name, ip, port));
                    }
                }
                break;
                case "stop": {
                    String name = arguments[1];
                    if (servers.containsKey(name)) {
                        servers.remove(name);
                        FoxServer.getInstance().getLogger().info(String.format("Deleting %s from the network.", name));
                    }
                }
                break;
                case "starts": {
                    if (FoxServer.getInstance().getConfig().isLogCommands()) {
                        FoxServer.getInstance().sendModerationMessage(ModChannel.INFORMATION,
                                new JsonModMessage()
                                        .setSender("Hydroangeas")
                                        .setMessage(arguments[1] + " Successfully started.")
                        );
                    }
                }
                break;
                default:
                    FoxServer.getInstance().getLogger().info(String.format("Unable to process %s, Invalid arguments", arguments[0]));
                break;
            }
        }
    }
}
