package com.foxprox.network.proxy.lib.console;

import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.ChatColor;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.core.api.chat.ComponentBuilder;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public final class WaterfallConsole extends SimpleTerminalConsole {

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        FoxServer proxy = FoxServer.getInstance();
        return super.buildReader(builder
                .appName(proxy.getName())
                .completer(new ConsoleCommandCompleter(proxy))
        );
    }

    @Override
    protected boolean isRunning() {
        return FoxProx.getInstance().isRunning;
    }

    @Override
    protected void runCommand(String command) {
        FoxServer proxy = FoxServer.getInstance();
        if (!proxy.getPluginManager().dispatchCommand(proxy.getConsole(), command)) {
            proxy.getConsole().sendMessage(new ComponentBuilder("Command not found").color(ChatColor.RED).create());
        }
    }

    @Override
    protected void shutdown() {
        FoxServer.getInstance().stop("The cat disconnected the cable.");
    }

}
