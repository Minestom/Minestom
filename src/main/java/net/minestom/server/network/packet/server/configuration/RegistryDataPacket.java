package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import static net.minestom.server.network.NetworkBuffer.NBT;

public record RegistryDataPacket(@NotNull NBTCompound data) implements ServerPacket {

    public RegistryDataPacket(@NotNull NetworkBuffer buffer) {
        this((NBTCompound) buffer.read(NBT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NBT, data);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_REGISTRY_DATA;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION);
        };
    }
}
