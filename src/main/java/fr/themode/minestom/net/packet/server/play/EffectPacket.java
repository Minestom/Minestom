package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.utils.BlockPosition;

public class EffectPacket implements ServerPacket {

    public int effectId;
    public BlockPosition position;
    public int data;
    public boolean disableRelativeVolume;

    @Override
    public void write(PacketWriter writer) {
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
