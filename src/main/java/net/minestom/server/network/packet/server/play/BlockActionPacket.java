package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BlockActionPacket implements ServerPacket {

    public Point blockPosition;
    public byte actionId;
    public byte actionParam;
    public int blockId;

    public BlockActionPacket(Point blockPosition, byte actionId, byte actionParam, int blockId) {
        this.blockPosition = blockPosition;
        this.actionId = actionId;
        this.actionParam = actionParam;
        this.blockId = blockId;
    }

    public BlockActionPacket(Point blockPosition, byte actionId, byte actionParam, Block block) {
        this(blockPosition, actionId, actionParam, block.id());
    }

    public BlockActionPacket() {
        this(Vec.ZERO, (byte) 0, (byte) 0, Block.AIR);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(actionId);
        writer.writeByte(actionParam);
        writer.writeVarInt(blockId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.actionId = reader.readByte();
        this.actionParam = reader.readByte();
        this.blockId = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_ACTION;
    }
}
