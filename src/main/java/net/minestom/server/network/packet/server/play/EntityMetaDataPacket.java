package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Metadata;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityMetaDataPacket(int entityId,
                                   @NotNull Map<Integer, Metadata.Entry<?>> entries) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public EntityMetaDataPacket {
        entries = Map.copyOf(entries);
    }

    public static final NetworkBuffer.Type<EntityMetaDataPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EntityMetaDataPacket value) {
            buffer.write(VAR_INT, value.entityId);
            for (var entry : value.entries.entrySet()) {
                buffer.write(BYTE, entry.getKey().byteValue());
                buffer.write(entry.getValue());
            }
            buffer.write(BYTE, (byte) 0xFF); // End
        }

        @Override
        public EntityMetaDataPacket read(@NotNull NetworkBuffer buffer) {
            return new EntityMetaDataPacket(buffer.read(VAR_INT), readEntries(buffer));
        }
    };

    private static Map<Integer, Metadata.Entry<?>> readEntries(@NotNull NetworkBuffer reader) {
        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>();
        while (true) {
            final byte index = reader.read(BYTE);
            if (index == (byte) 0xFF) { // reached the end
                break;
            }
            final int type = reader.read(VAR_INT);
            entries.put((int) index, Metadata.Entry.read(type, reader));
        }
        return entries;
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
            final var t = value.type();
            final var v = value.value();

            if (v instanceof Component c) {
                var translated = operator.apply(c);
                entries.put(key, t == Metadata.TYPE_OPT_CHAT ? Metadata.OptChat(translated) : Metadata.Chat(translated));
            } else {
                entries.put(key, value);
            }
        });

        return new EntityMetaDataPacket(this.entityId, entries);
    }
}
