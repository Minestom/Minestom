package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.resourcepack.ResourcePackStatus;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientResourcePackStatusPacket(@NotNull ResourcePackStatus status) implements ClientPacket {
    public ClientResourcePackStatusPacket(BinaryReader reader) {
        this(ResourcePackStatus.values()[reader.readVarInt()]);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(status.ordinal());
    }
}
