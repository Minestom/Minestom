package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Entity;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record CameraPacket(int cameraId) implements ServerPacket {
    public CameraPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT));
    }

    public CameraPacket(@NotNull Entity camera) {
        this(camera.getEntityId());
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, cameraId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CAMERA;
    }
}
