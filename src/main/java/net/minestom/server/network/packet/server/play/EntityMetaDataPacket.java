package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Metadata;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class EntityMetaDataPacket implements ServerPacket {

    public int entityId;
    public Collection<Metadata.Entry<?>> entries;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);

        // Write all the fields
        for (Metadata.Entry<?> entry : entries) {
            entry.write(writer);
        }

        writer.writeByte((byte) 0xFF); // End
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }
}
