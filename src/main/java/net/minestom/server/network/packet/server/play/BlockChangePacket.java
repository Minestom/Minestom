package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BlockChangePacket implements ServerPacket {

    public BlockPosition blockPosition;
    public int blockStateId;

    public BlockChangePacket() {
        blockPosition = new BlockPosition(0,0,0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockStateId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        blockPosition = reader.readBlockPosition();
        blockStateId = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_CHANGE;
    }
}
