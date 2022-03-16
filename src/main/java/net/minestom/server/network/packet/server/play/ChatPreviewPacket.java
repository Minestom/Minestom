package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

public record ChatPreviewPacket(int queryId, @Nullable Component preview) implements ComponentHoldingServerPacket {
    public ChatPreviewPacket(BinaryReader reader) {
        this(reader.readInt(), reader.readBoolean() ? reader.readComponent() : null);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(queryId);
        writer.writeBoolean(preview != null);
        if (preview != null) writer.writeComponent(preview);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_PREVIEW;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return Collections.singleton(preview);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ChatPreviewPacket(queryId, operator.apply(preview));
    }
}
