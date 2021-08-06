package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.resourcepack.ResourcePackStatus;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientResourcePackStatusPacket extends ClientPlayPacket {

    public ResourcePackStatus result = ResourcePackStatus.SUCCESS;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.result = ResourcePackStatus.values()[reader.readVarInt()];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(result.ordinal());
    }
}
