package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientPongPacket(int id) implements ClientPacket {
    public ClientPongPacket(BinaryReader reader) {
        this(reader.readInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(id);
    }
}
