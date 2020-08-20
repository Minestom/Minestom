package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryWriter;

public class EffectPacket implements ServerPacket {

    public int effectId;
    public BlockPosition position;
    public int data;
    public boolean disableRelativeVolume;

    @Override
    public void write(BinaryWriter writer) {
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
