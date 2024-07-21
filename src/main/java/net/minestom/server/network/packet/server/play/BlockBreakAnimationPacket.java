package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockBreakAnimationPacket(int entityId, @NotNull Point blockPosition,
                                        byte destroyStage) implements ServerPacket.Play {
    public BlockBreakAnimationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(BLOCK_POSITION), reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(BLOCK_POSITION, blockPosition);
        writer.write(BYTE, destroyStage);
    }

}