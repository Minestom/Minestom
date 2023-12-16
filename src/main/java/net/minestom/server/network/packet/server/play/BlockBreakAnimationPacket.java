package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockBreakAnimationPacket(int entityId, @NotNull Point blockPosition,
                                        byte destroyStage) implements ServerPacket {
    public BlockBreakAnimationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(BLOCK_POSITION), reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(BLOCK_POSITION, blockPosition);
        writer.write(BYTE, destroyStage);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.BLOCK_BREAK_ANIMATION;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}