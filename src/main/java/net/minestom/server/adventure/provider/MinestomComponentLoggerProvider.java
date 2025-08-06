package net.minestom.server.adventure.provider;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public class MinestomComponentLoggerProvider implements ComponentLoggerProvider {
    private static final ANSIComponentSerializer SERIALIZER = ANSIComponentSerializer.ansi();

    @Override
    public @NotNull ComponentLogger logger(@NotNull LoggerHelper helper, @NotNull String name) {
        return helper.delegating(LoggerFactory.getLogger(name), SERIALIZER::serialize);
    }
}
