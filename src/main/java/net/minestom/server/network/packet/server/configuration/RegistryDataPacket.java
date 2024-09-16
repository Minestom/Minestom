package net.minestom.server.network.packet.server.configuration;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
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
    public static final NetworkBuffer.Type<RegistryDataPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, RegistryDataPacket::registryId,
            Entry.SERIALIZER.list(Integer.MAX_VALUE), RegistryDataPacket::entries,
            RegistryDataPacket::new);

    public record Entry(
            @NotNull String id,
            @Nullable BinaryTag data
    ) {
        public static final NetworkBuffer.Type<Entry> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Entry::id, NBT.optional(), Entry::data, Entry::new);
    }
}
