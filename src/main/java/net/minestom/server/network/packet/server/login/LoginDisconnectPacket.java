package net.minestom.server.network.packet.server.login;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class LoginDisconnectPacket implements ComponentHoldingServerPacket {
    public Component kickMessage;

    private LoginDisconnectPacket() {
        this(Component.text("This constructor should not be used, tell your server devs."));
    }

    public LoginDisconnectPacket(@NotNull Component kickMessage) {
        this.kickMessage = kickMessage;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(kickMessage);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        kickMessage = reader.readComponent();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_DISCONNECT;
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
