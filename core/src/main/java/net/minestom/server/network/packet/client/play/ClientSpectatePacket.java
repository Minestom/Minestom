package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClientSpectatePacket extends ClientPlayPacket {

    public UUID targetUuid = new UUID(0,0);

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.targetUuid = reader.readUuid();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(targetUuid);
    }
}
