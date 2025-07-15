package net.minestom.server.adventure.provider;

import java.util.function.Consumer;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public final class MinestomPlainTextComponentSerializerProvider implements PlainTextComponentSerializer.Provider {
    @Override
    public PlainTextComponentSerializer plainTextSimple() {
        return PlainTextComponentSerializer.builder()
                .flattener(MinestomFlattenerProvider.INSTANCE)
                .build();
    }

    @Override
    public Consumer<PlainTextComponentSerializer.Builder> plainText() {
        // we will provide our flattener to allow for custom translations/etc
        return builder -> builder.flattener(MinestomFlattenerProvider.INSTANCE);
    }
}
