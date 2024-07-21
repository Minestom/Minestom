package net.minestom.server.network.packet.server.configuration;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.NBT;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record RegistryDataPacket(
        @NotNull String registryId,
        @NotNull List<Entry> entries
) implements ServerPacket.Configuration {

    public RegistryDataPacket(@NotNull NetworkBuffer buffer) {
        this(buffer.read(STRING), buffer.readCollection(Entry::new, Integer.MAX_VALUE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, registryId);
        writer.writeCollection(entries);
    }

    public record Entry(
            @NotNull String id,
            @Nullable CompoundBinaryTag data
    ) implements NetworkBuffer.Writer {

        public Entry(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), (CompoundBinaryTag) reader.readOptional(NBT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, id);
            writer.writeOptional(NBT, data);
        }
    }
}
