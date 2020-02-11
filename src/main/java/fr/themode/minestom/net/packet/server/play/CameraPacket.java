package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class CameraPacket implements ServerPacket {

    public int cameraId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(cameraId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CAMERA;
    }
}
