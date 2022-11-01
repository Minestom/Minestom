package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EffectPacket(int effectId, Point position, int data,
                           boolean disableRelativeVolume) implements ServerPacket {
    public EffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(BLOCK_POSITION), reader.read(INT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, effectId);
        writer.write(BLOCK_POSITION, position);
        writer.write(INT, data);
        writer.write(BOOLEAN, disableRelativeVolume);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EFFECT;
    }
}
