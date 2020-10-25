package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientSteerVehiclePacket extends ClientPlayPacket {

    public float sideways;
    public float forward;
    public byte flags;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.sideways = reader.readFloat();
        this.forward = reader.readFloat();
        this.flags = reader.readByte();
    }
}
