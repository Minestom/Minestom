package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientSteerBoatPacket extends ClientPlayPacket {

    public boolean leftPaddleTurning;
    public boolean rightPaddleTurning;

    @Override
    public void read(PacketReader reader) {
        this.leftPaddleTurning = reader.readBoolean();
        this.rightPaddleTurning = reader.readBoolean();
    }
}
