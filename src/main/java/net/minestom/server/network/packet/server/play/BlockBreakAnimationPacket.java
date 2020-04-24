package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;

public class BlockBreakAnimationPacket implements ServerPacket {

    public int entityId;
    public BlockPosition blockPosition;
    public byte destroyStage;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(destroyStage);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_BREAK_ANIMATION;
    }
}