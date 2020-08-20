package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientSteerBoatPacket extends ClientPlayPacket {

    public boolean leftPaddleTurning;
    public boolean rightPaddleTurning;

    @Override
    public void read(BinaryReader reader) {
        this.leftPaddleTurning = reader.readBoolean();
        this.rightPaddleTurning = reader.readBoolean();
    }
}
