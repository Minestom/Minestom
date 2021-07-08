package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Metadata;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;

public class EntityMetaDataPacket implements ComponentHoldingServerPacket {

    public int entityId;
    public Collection<Metadata.Entry<?>> entries;

    public EntityMetaDataPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);

        if(entries != null) {
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
        while(true) {
            byte index = reader.readByte();

            if(index == (byte) 0xFF) { // reached the end
                break;
            }

            entries.add(new Metadata.Entry<>(reader));
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }

    @Override
    public @NotNull Collection<Component> components() {
        List<Component> components = new ArrayList<>();
        for (Metadata.Entry<?> entry : entries) {
            int type = entry.getMetaValue().getType();
            if (type == Metadata.TYPE_CHAT || type == Metadata.TYPE_OPTCHAT) {
                components.add(((Metadata.Entry<Component>) entry).getMetaValue().getValue());
            }
        }
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        EntityMetaDataPacket packet = new EntityMetaDataPacket();
        packet.entityId = this.entityId;
        packet.entries = new ArrayList<>();
        for (Metadata.Entry<?> entry : this.entries) {
            int type = entry.getMetaValue().getType();
            Metadata.Value<Component> value;
            if (type == Metadata.TYPE_OPTCHAT) {
                value = Metadata.OptChat(operator.apply(((Metadata.Entry<Component>) entry).getMetaValue().getValue()));
            }
            else if (type == Metadata.TYPE_CHAT) {
                value = Metadata.Chat(operator.apply(((Metadata.Entry<Component>) entry).getMetaValue().getValue()));
            }
            else continue;
            packet.entries.add(new Metadata.Entry<>(entry.getIndex(), value));
        }
        return packet;
    }
}
