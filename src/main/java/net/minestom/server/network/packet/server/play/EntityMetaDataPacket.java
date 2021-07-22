package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Metadata;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class EntityMetaDataPacket implements ServerPacket {

    public int entityId;
    public Collection<Metadata.Entry<?>> entries;

    public EntityMetaDataPacket(int entityId, Collection<Metadata.Entry<?>> entries) {
        this.entityId = entityId;
        this.entries = entries;
    }

    public EntityMetaDataPacket() {
        this(0, Collections.emptyList());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);

        if (entries != null) {
            // Write all the fields
            for (Metadata.Entry<?> entry : entries) {
                entry.write(writer);
            }
        }

        writer.writeByte((byte) 0xFF); // End
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();

        entries = new LinkedList<>();
        while (true) {
            byte index = reader.readByte();

            if (index == (byte) 0xFF) { // reached the end
                break;
            }

            entries.add(new Metadata.Entry<>(reader));
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }
}
