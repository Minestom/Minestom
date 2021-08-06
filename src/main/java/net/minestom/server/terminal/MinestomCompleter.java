package net.minestom.server.terminal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Map;

public class MinestomCompleter implements Completer {

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        Map<String, Command> commands = MinecraftServer.getCommandManager().getDispatcher().getCommandMap();

        for (String commandName : commands.keySet()) {
            candidates.add(new Candidate(commandName));
        }
    }


}
