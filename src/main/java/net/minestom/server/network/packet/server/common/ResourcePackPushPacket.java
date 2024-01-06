package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
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
) implements ComponentHoldingServerPacket {
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
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_RESOURCE_PACK_PUSH_PACKET;
            case PLAY -> ServerPacketIdentifier.RESOURCE_PACK_PUSH;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION, ConnectionState.PLAY);
        };
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
