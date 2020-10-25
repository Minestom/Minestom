package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientHeldItemChangePacket extends ClientPlayPacket {

    public short slot;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.slot = reader.readShort();
    }
}
