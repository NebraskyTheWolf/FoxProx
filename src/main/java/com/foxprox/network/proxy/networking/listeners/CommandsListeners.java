package com.foxprox.network.proxy.networking.listeners;

import com.foxprox.network.proxy.core.api.event.PubSubMessage;
import com.foxprox.network.proxy.core.api.plugin.Listener;
import com.foxprox.network.proxy.samagames.api.pubsub.PendingMessage;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.event.EventHandler;

public class CommandsListeners implements Listener {

    @EventHandler
    public void onCommand(PubSubMessage event) {
        if (event.getChannel().equals("command.servers.all")) {
            if (event.getMessage().split(" ")[1].equals("stop")) {
                event.getPubSub().send(new PendingMessage("response.samacli", "server FoxProx SHUTDOWN", new Runnable() {
                    @Override
                    public void run() {
                        FoxServer.getInstance().stop("STOPPED BY CLI ADMINISTRATOR REQUEST ID: " + event.getMessage().split(" ")[2]);
                    }
                }));
            }
        }
    }
}
