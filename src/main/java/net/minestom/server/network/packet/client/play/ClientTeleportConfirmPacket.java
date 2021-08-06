package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientTeleportConfirmPacket extends ClientPlayPacket {

    public int teleportId;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.teleportId = reader.readVarInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(teleportId);
    }
}
