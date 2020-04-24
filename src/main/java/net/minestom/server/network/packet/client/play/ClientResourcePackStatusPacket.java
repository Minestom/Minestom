package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientResourcePackStatusPacket extends ClientPlayPacket {

    public Result result;

    @Override
    public void read(PacketReader reader) {
        this.result = Result.values()[reader.readVarInt()];
    }

    public enum Result {
        SUCCESS, DECLINED, FAILED_DOWNLOAD, ACCEPTED
    }

}
