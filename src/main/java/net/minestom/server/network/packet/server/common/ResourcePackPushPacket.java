package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record ResourcePackPushPacket(
        @NotNull UUID id,
        @NotNull String url,
        @NotNull String hash,
        boolean forced,
        @Nullable Component prompt
) implements ServerPacket.Configuration, ServerPacket.Play, ServerPacket.ComponentHolding {
    public ResourcePackPushPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(UUID), reader.read(STRING), reader.read(STRING),
                reader.read(BOOLEAN), reader.readOptional(COMPONENT));
    }

    public ResourcePackPushPacket(@NotNull ResourcePackInfo resourcePackInfo, boolean required, @Nullable Component prompt) {
        this(resourcePackInfo.id(), resourcePackInfo.uri().toString(), resourcePackInfo.hash(), required, prompt);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(UUID, id);
        writer.write(STRING, url);
        writer.write(STRING, hash);
        writer.write(BOOLEAN, forced);
        writer.writeOptional(COMPONENT, prompt);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.prompt);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ResourcePackPushPacket(this.id, this.url, this.hash, this.forced, operator.apply(this.prompt));
    }
}
