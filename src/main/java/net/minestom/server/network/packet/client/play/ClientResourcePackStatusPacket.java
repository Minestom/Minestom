package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.resourcepack.ResourcePackStatus;

public class ClientResourcePackStatusPacket extends ClientPlayPacket {

    public ResourcePackStatus result;

    @Override
    public void read(PacketReader reader) {
        this.result = ResourcePackStatus.values()[reader.readVarInt()];
    }

}
