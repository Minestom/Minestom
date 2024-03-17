package net.minestom.server.network.packet.server.configuration;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.NBT;

public record RegistryDataPacket(@NotNull CompoundBinaryTag data) implements ServerPacket.Configuration {
    public RegistryDataPacket(@NotNull NetworkBuffer buffer) {
        this((CompoundBinaryTag) buffer.read(NBT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NBT, data);
    }

    @Override
    public int configurationId() {
        return ServerPacketIdentifier.CONFIGURATION_REGISTRY_DATA;
    }
}
