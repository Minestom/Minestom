package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record OpenSignEditorPacket(@NotNull Point position, boolean isFrontText) implements ServerPacket {
    public OpenSignEditorPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BLOCK_POSITION), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BLOCK_POSITION, position);
        writer.write(BOOLEAN, isFrontText);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_SIGN_EDITOR;
    }
}
