package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.resourcepack.ResourcePackStatus;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientResourcePackStatusPacket extends ClientPlayPacket {

    public ResourcePackStatus result;

    @Override
    public void read(BinaryReader reader) {
        this.result = ResourcePackStatus.values()[reader.readVarInt()];
    }

}
