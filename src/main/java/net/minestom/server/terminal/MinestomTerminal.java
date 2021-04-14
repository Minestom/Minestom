package net.minestom.server.terminal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import org.jetbrains.annotations.ApiStatus;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class MinestomTerminal {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();
    private static final String PROMPT = "> ";

    private static volatile boolean running = false;

    @ApiStatus.Internal
    public static void start() {
        Terminal terminal = null;
        try {
            terminal = TerminalBuilder.terminal();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        running = true;
        while (running) {
            String command;
            try {
                command = reader.readLine(PROMPT);
                COMMAND_MANAGER.execute(COMMAND_MANAGER.getConsoleSender(), command);
            } catch (UserInterruptException e) {
                // Ignore
            } catch (EndOfFileException e) {
                return;
            }
        }
    }

    @ApiStatus.Internal
    public static void stop() {
        running = false;
    }

}
