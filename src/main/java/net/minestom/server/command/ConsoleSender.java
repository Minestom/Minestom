package net.minestom.server.command;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the console when sending a command to the server.
 */
public class ConsoleSender implements CommandSender {
    private static final ComponentLogger LOGGER = ComponentLogger.logger(ConsoleSender.class);

    private final TagHandler tagHandler = TagHandler.newHandler();

    private final Identity identity = Identity.nil();
    private final Pointers pointers = Pointers.builder()
            .withStatic(Identity.UUID, this.identity.uuid())
            .build();

    @Override
    public void sendMessage(@NotNull String message) {
        LOGGER.info(message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        LOGGER.info(message);
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    @Override
    public @NotNull Identity identity() {
        return this.identity;
    }

    @Override
    public @NotNull Pointers pointers() {
        return this.pointers;
    }
}
