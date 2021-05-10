package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BlockBreakAnimationPacket implements ServerPacket {

    public int entityId;
    public BlockPosition blockPosition;
    public byte destroyStage;

    public BlockBreakAnimationPacket() {
        blockPosition = new BlockPosition(0,0,0);
    }

    public BlockBreakAnimationPacket(int entityId, BlockPosition blockPosition, byte destroyStage) {
        this.entityId = entityId;
        this.blockPosition = blockPosition;
        this.destroyStage = destroyStage;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(destroyStage);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        blockPosition = reader.readBlockPosition();
        destroyStage = reader.readByte();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_BREAK_ANIMATION;
    }
}