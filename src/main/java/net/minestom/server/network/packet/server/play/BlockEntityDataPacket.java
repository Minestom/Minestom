package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public record BlockEntityDataPacket(@NotNull Point blockPosition, int action,
                                    @Nullable NBTCompound data) implements ServerPacket {
    public BlockEntityDataPacket(BinaryReader reader) {
        this(reader.readBlockPosition(), reader.readVarInt(), (NBTCompound) reader.readTag());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(action);
        if (data != null) {
            writer.writeNBT("", data);
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
