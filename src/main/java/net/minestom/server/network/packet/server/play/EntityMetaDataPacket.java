package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Metadata;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityMetaDataPacket(int entityId,
                                   @NotNull Map<Integer, Metadata.Entry<?>> entries) implements ComponentHoldingServerPacket {
    public EntityMetaDataPacket {
        entries = Map.copyOf(entries);
    }

    public EntityMetaDataPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), readEntries(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        for (var entry : entries.entrySet()) {
            writer.write(BYTE, entry.getKey().byteValue());
            writer.write(entry.getValue());
        }
        writer.write(BYTE, (byte) 0xFF); // End
    }

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
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return this.entries.values()
                .stream()
                .map(Metadata.Entry::value)
                .filter(entry -> entry instanceof Component || entry instanceof ItemStack)
                .flatMap(entry -> entry instanceof Component component
                        ? Stream.ofNullable(component)
                        : Stream.concat(((ItemStack) entry).getLore().stream(),
                        Stream.ofNullable(((ItemStack) entry).getDisplayName())))
                .toList();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        final var entries = new HashMap<Integer, Metadata.Entry<?>>();

        this.entries.forEach((key, value) -> {
            final var v = value.value();

            if(v instanceof ItemStack item) {
                value = Metadata.Slot(item.withDisplayName(operator).withLore(lines -> {
                    lines.replaceAll(operator);

                    return lines;
                }));
            } else if(v instanceof Component component) {
                component = operator.apply(component);

                value = value.type() == Metadata.TYPE_OPTCHAT ? Metadata.OptChat(component) : Metadata.Chat(component);
            }

            entries.put(key, value);
        });

        return new EntityMetaDataPacket(this.entityId, entries);
    }
}
