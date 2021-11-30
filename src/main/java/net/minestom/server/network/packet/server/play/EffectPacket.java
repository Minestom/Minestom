package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EffectPacket(int effectId, Point position, int data,
                           boolean disableRelativeVolume) implements ServerPacket {
    public EffectPacket(BinaryReader reader) {
        this(reader.readInt(), reader.readBlockPosition(), reader.readInt(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(effectId);
        writer.writeBlockPosition(position);
        writer.writeInt(data);
        writer.writeBoolean(disableRelativeVolume);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EFFECT;
    }
}
