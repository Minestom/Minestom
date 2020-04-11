package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientSteerBoatPacket extends ClientPlayPacket {

    public boolean leftPaddleTurning;
    public boolean rightPaddleTurning;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readBoolean(value -> leftPaddleTurning = value);
        reader.readBoolean(value -> {
            rightPaddleTurning = value;
            callback.run();
        });
    }
}
