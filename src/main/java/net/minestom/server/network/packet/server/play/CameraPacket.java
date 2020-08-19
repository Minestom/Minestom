package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class CameraPacket implements ServerPacket {

    public int cameraId;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(cameraId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CAMERA;
    }
}
