package net.minestom.server.terminal;

import org.fusesource.jansi.AnsiConsole;
import org.tinylog.core.LogEntry;
import org.tinylog.writers.AbstractFormatPatternWriter;

import java.util.Map;

import static net.minestom.server.terminal.MinestomTerminal.reader;

public final class MinestomConsoleWriter extends AbstractFormatPatternWriter {
    public MinestomConsoleWriter(Map<String, String> properties) {
        super(properties);
    }

    @Override
    public void write(LogEntry logEntry) throws Exception {
        String rendered = render(logEntry);
        if (reader != null) {
            reader.printAbove(TerminalColorConverter.format(rendered, false));
        } else {
            AnsiConsole.out().print(TerminalColorConverter.format(rendered, false));
        }
    }

    @Override
    public void flush() {
        // EMPTY
    }

    @Override
    public void close() throws Exception {
        // EMPTY
    }
}
