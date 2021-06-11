package net.minestom.server.adventure.provider;

import java.util.function.Consumer;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public final class MinestomLegacyComponentSerializerProvider implements LegacyComponentSerializer.Provider {
    @Override
    public @NotNull LegacyComponentSerializer legacyAmpersand() {
        return LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.AMPERSAND_CHAR)
                .flattener(MinestomFlattenerProvider.INSTANCE)
                .build();
    }

    @Override
    public @NotNull LegacyComponentSerializer legacySection() {
        return LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .flattener(MinestomFlattenerProvider.INSTANCE)
                .build();
    }

    @Override
    public @NotNull Consumer<LegacyComponentSerializer.Builder> legacy() {
        // we will provide our flattener to allow for custom translations/etc
        return builder -> builder.flattener(MinestomFlattenerProvider.INSTANCE);
    }
}
