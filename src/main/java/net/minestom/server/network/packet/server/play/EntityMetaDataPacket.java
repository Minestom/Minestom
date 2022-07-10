package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Metadata;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record EntityMetaDataPacket(int entityId,
                                   @NotNull Map<Integer, Metadata.Entry<?>> entries) implements ServerPacket {
    public EntityMetaDataPacket {
        entries = Map.copyOf(entries);
    }

    public EntityMetaDataPacket(BinaryReader reader) {
        this(reader.readVarInt(), readEntries(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        for (var entry : entries.entrySet()) {
            writer.writeByte(entry.getKey().byteValue());
            writer.write(entry.getValue());
        }
        writer.writeByte((byte) 0xFF); // End
    }

    private static Map<Integer, Metadata.Entry<?>> readEntries(BinaryReader reader) {
        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>();
        while (true) {
            final byte index = reader.readByte();
            if (index == (byte) 0xFF) { // reached the end
                break;
            }
            final int type = reader.readVarInt();
            entries.put((int) index, Metadata.Entry.read(type, reader));
        }
        return entries;
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }
}
