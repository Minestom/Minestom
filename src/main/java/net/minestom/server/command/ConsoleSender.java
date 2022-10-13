package net.minestom.server.command;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.permission.ForwardingPermissionHandler;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.permission.PermissionHandlerImpl;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the console when sending a command to the server.
 */
public class ConsoleSender implements CommandSender, ForwardingPermissionHandler {
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleSender.class);

    private PermissionHandler permissionHandler = new PermissionHandlerImpl();
    private final TagHandler tagHandler = TagHandler.newHandler();

    @Override
    public void sendMessage(@NotNull String message) {
        LOGGER.info(message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        // we don't use the serializer here as we just need the plain text of the message
        this.sendMessage(PLAIN_SERIALIZER.serialize(message));
    }

    @Override
    public @NotNull PermissionHandler getPermissionHandler() {
        return this.permissionHandler;
    }

    public void setPermissionHandler(@NotNull PermissionHandler permissionHandler) {
        this.permissionHandler = permissionHandler;
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
