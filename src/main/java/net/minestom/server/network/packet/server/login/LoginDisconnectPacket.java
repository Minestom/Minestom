package net.minestom.server.network.packet.server.login;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.JSON_COMPONENT;

public record LoginDisconnectPacket(@NotNull Component kickMessage) implements ComponentHoldingServerPacket {
    public LoginDisconnectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(JSON_COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(JSON_COMPONENT, kickMessage);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case LOGIN -> ServerPacketIdentifier.LOGIN_DISCONNECT;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.LOGIN);
        };
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.kickMessage);
    }

    @Override
    public @NotNull LoginDisconnectPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new LoginDisconnectPacket(operator.apply(this.kickMessage));
    }
}
