package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record CameraPacket(int cameraId) implements ServerPacket {
    public CameraPacket(BinaryReader reader) {
        this(reader.readVarInt());
    }

    public CameraPacket(@NotNull Entity camera) {
        this(camera.getEntityId());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(cameraId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CAMERA;
    }
}
