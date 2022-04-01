package net.minestom.server.terminal

import net.minestom.server.MinecraftServer.Companion.commandManager
import java.lang.Runnable
import net.minestom.server.terminal.MinestomTerminal
import java.io.IOException
import net.minestom.server.command.CommandManager
import net.minestom.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

object MinestomTerminal {
    private const val PROMPT = "> "

    @Volatile
    private var terminal: Terminal? = null

    @Volatile
    private var running = false
    @ApiStatus.Internal
    fun start() {
        val thread = Thread(null, label@ Runnable {
            try {
                terminal = TerminalBuilder.terminal()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build()
            running = true
            while (running) {
                var command: String?
                try {
                    command = reader.readLine(PROMPT)
                    val commandManager = commandManager
                    commandManager.execute(commandManager.consoleSender, command)
                } catch (e: UserInterruptException) {
                    // Handle Ctrl + C
                    System.exit(0)
                    return@label
                } catch (e: EndOfFileException) {
                    return@label
                }
            }
        }, "Jline")
        thread.isDaemon = true
        thread.start()
    }

    @ApiStatus.Internal
    fun stop() {
        running = false
        if (terminal != null) {
            try {
                terminal!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}