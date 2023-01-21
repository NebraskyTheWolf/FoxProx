package com.foxprox.network.proxy.lib.console;

import com.foxprox.network.proxy.core.api.FoxServer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

final class ConsoleCommandCompleter implements Completer {

    private final FoxServer proxy;

    ConsoleCommandCompleter(FoxServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<String> suggestions = this.proxy.getPluginManager().tabCompleteCommand(this.proxy.getConsole(), line.line());
        if (suggestions.isEmpty()) {
            return;
        }

        for (String suggestion : suggestions) {
            candidates.add(new Candidate(suggestion));
        }
    }

}
