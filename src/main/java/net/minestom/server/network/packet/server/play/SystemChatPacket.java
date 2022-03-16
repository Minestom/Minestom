package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

public record SystemChatPacket(@NotNull Component message, int typeId) implements ComponentHoldingServerPacket {
    public SystemChatPacket(BinaryReader reader) {
        this(reader.readComponent(), reader.readVarInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(message);
        writer.writeVarInt(typeId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SYSTEM_CHAT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return Collections.singleton(message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SystemChatPacket(operator.apply(message), typeId);
    }
}
