package net.minestom.server.adventure.provider;

import java.util.function.Consumer;

import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public class MinestomAnsiComponentSerializerProvider implements ANSIComponentSerializer.Provider {
    @Override
    public ANSIComponentSerializer ansi() {
        final ANSIComponentSerializer.Builder builder = ANSIComponentSerializer.builder();
        this.builder().accept(builder);
        return builder.build();
    }

    @Override
    public Consumer<ANSIComponentSerializer.Builder> builder() {
        return builder -> builder.flattener(MinestomFlattenerProvider.INSTANCE);
    }
}
