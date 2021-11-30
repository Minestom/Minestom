package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Metadata;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record EntityMetaDataPacket(int entityId,
                                   @NotNull Collection<Metadata.Entry<?>> entries) implements ServerPacket {
    public EntityMetaDataPacket {
        entries = List.copyOf(entries);
    }

    public EntityMetaDataPacket(BinaryReader reader) {
        this(reader.readVarInt(), readEntries(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        this.entries.forEach(writer::write);
        writer.writeByte((byte) 0xFF); // End
    }

    private static Collection<Metadata.Entry<?>> readEntries(BinaryReader reader) {
        Collection<Metadata.Entry<?>> entries = new ArrayList<>();
        while (true) {
            byte index = reader.readByte();
            if (index == (byte) 0xFF) { // reached the end
                break;
            }
            entries.add(new Metadata.Entry<>(reader));
        }
        return entries;
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }
}
