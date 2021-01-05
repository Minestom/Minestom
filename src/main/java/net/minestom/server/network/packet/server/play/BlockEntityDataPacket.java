package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class BlockEntityDataPacket implements ServerPacket {

    public BlockPosition blockPosition;
    public byte action;
    public NBTCompound nbtCompound;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(action);
        if (nbtCompound != null) {
            writer.writeNBT("", nbtCompound);
        } else {
            // TAG_End
            writer.writeByte((byte) 0x00);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_ENTITY_DATA;
    }
}
