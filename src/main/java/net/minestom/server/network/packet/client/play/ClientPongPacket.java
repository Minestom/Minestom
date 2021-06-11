package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientPongPacket extends ClientPlayPacket {

    public int id;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.id = reader.readInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(id);
    }
}
