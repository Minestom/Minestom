package net.minestom.server.terminal;

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
        if (reader != null) {
            reader.printAbove(render(logEntry));
        } else {
            System.out.print(render(logEntry));
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
