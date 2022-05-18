package net.minestom.server.terminal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.listener.TabCompleteListener;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class MinestomCompleter implements Completer {
    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        final var commandManager = MinecraftServer.getCommandManager();
        final var consoleSender = commandManager.getConsoleSender();
        if (line.wordIndex() == 0) {
            final String commandString = line.word().toLowerCase();
            candidates.addAll(
                    commandManager.getDispatcher().getCommands().stream()
                            .map(Command::getName)
                            .filter(name -> commandString.isBlank() || name.toLowerCase().startsWith(commandString))
                            .map(Candidate::new)
                            .toList()
            );
        } else {
            final String text = line.line();
            TabCompleteListener.getSuggestion(consoleSender, text)
                    .ifPresent(suggestion ->
                            suggestion.getEntries().stream()
                                    .map(SuggestionEntry::getEntry)
                                    .map(Candidate::new)
                                    .forEach(candidates::add)
                    );
        }
    }
}
