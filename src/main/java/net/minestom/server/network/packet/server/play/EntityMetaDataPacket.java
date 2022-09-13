package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Metadata;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public record EntityMetaDataPacket(int entityId,
                                   @NotNull Map<Integer, Metadata.Entry<?>> entries) implements ComponentHoldingServerPacket {
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

    @Override
    public @NotNull Collection<Component> components() {
        return this.entries.values()
                .stream()
                .map(Metadata.Entry::value)
                .filter(entry -> entry instanceof Component)
                .map(entry -> (Component) entry)
                .toList();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        final var entries = new HashMap<Integer, Metadata.Entry<?>>();

        this.entries.forEach((key, value) -> {
            final var v = value.value();

            entries.put(key, v instanceof Component c ? Metadata.OptChat(operator.apply(c)) : value);
        });

        return new EntityMetaDataPacket(this.entityId, entries);
    }
}
