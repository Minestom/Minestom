package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientPlayerRotationPacket extends ClientPlayPacket {

    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.yaw = reader.readFloat();
        this.pitch = reader.readFloat();
        this.onGround = reader.readBoolean();
    }
}
