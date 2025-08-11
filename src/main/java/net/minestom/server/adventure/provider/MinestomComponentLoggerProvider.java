package net.minestom.server.adventure.provider;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public class MinestomComponentLoggerProvider implements ComponentLoggerProvider {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .flattener(MinestomFlattenerProvider.INSTANCE)
            .hexColors()
            .build();

    @Override
    public ComponentLogger logger(LoggerHelper helper, String name) {
        return helper.delegating(LoggerFactory.getLogger(name), SERIALIZER::serialize);
    }
}
