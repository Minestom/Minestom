package net.minestom.server.command;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents the console when sending a command to the server.
 */
public class ConsoleSender implements CommandSender {
    private static final ComponentLogger LOGGER = ComponentLogger.logger(ConsoleSender.class);

    private final Set<Permission> permissions = new CopyOnWriteArraySet<>();
    private final TagHandler tagHandler = TagHandler.newHandler();

    @Override
    public void sendMessage(@NotNull String message) {
        LOGGER.info(message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        LOGGER.info(message);
    }

    @NotNull
    @Override
    public Set<Permission> getAllPermissions() {
        return permissions;
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public ConsoleSender asConsole() {
        return this;
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }
}
