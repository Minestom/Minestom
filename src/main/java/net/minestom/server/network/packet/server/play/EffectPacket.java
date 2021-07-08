package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class EffectPacket implements ServerPacket {

    public int effectId;
    public Point position;
    public int data;
    public boolean disableRelativeVolume;

    public EffectPacket() {
        position = Vec.ZERO;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(effectId);
        writer.writeBlockPosition(position);
        writer.writeInt(data);
        writer.writeBoolean(disableRelativeVolume);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.effectId = reader.readInt();
        this.position = reader.readBlockPosition();
        this.data = reader.readInt();
        this.disableRelativeVolume = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EFFECT;
    }
}
