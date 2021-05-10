package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientPlayerPacket extends ClientPlayPacket {

    public boolean onGround;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.onGround = reader.readBoolean();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(onGround);
    }
}
