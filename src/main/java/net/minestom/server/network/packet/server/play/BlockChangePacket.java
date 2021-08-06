package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BlockChangePacket implements ServerPacket {

    public Point blockPosition;
    public int blockStateId;

    public BlockChangePacket(Point blockPosition, int blockStateId) {
        this.blockPosition = blockPosition;
        this.blockStateId = blockStateId;
    }

    public BlockChangePacket(Point blockPosition, Block block) {
        this(blockPosition, block.stateId());
    }

    public BlockChangePacket() {
        this(Vec.ZERO, 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockStateId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.blockStateId = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_CHANGE;
    }
}
