package net.minestom.server.terminal;

import java.util.Map;

import org.tinylog.core.LogEntry;
import org.tinylog.writers.AbstractFormatPatternWriter;

public final class MinestomConsoleWriter extends AbstractFormatPatternWriter {
    public MinestomConsoleWriter(Map<String, String> properties) {
        super(properties);
    }

    @Override
    public void write(LogEntry logEntry) throws Exception {
        if (MinestomTerminal.getReader() != null) {
            MinestomTerminal.getReader().printAbove(render(logEntry));
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
