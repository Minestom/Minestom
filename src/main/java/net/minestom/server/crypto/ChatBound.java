package net.minestom.server.crypto;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ChatBound(int chatType, Component name, @Nullable Component targetName) implements NetworkBuffer.Writer {
    public ChatBound(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(COMPONENT), reader.readOptional(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, chatType);
        writer.write(COMPONENT, name);
        writer.writeOptional(COMPONENT, targetName);
    }
}
