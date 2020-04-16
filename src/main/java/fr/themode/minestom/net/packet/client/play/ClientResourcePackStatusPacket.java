package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

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
