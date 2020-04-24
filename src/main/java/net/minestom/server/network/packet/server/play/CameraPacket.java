package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

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
