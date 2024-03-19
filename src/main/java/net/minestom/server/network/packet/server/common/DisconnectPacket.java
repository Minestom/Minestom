package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record DisconnectPacket(@NotNull Component message) implements ServerPacket.Configuration, ServerPacket.Play,
        ServerPacket.ComponentHolding {
    public DisconnectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(COMPONENT, message);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_DISCONNECT;
            case PLAY -> ServerPacketIdentifier.DISCONNECT;
            default ->
                    PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION, ConnectionState.PLAY);
        };
    }

    @Override
    public int configurationId() {
        return ServerPacketIdentifier.CONFIGURATION_DISCONNECT;
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.DISCONNECT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new DisconnectPacket(operator.apply(message));
    }
}
