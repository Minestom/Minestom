package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record ChatPreviewPacket(int queryId, @Nullable Component preview) implements ComponentHoldingServerPacket {
    public ChatPreviewPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.readOptional(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, queryId);
        writer.writeOptional(COMPONENT, preview);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHAT_PREVIEW;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(preview);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ChatPreviewPacket(queryId, operator.apply(preview));
    }
}
