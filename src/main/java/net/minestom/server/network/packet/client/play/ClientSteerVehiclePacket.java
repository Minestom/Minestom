package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientSteerVehiclePacket(float sideways, float forward,
                                       byte flags) implements ClientPacket {
    public ClientSteerVehiclePacket(BinaryReader reader) {
        this(reader.readFloat(), reader.readFloat(), reader.readByte());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(sideways);
        writer.writeFloat(forward);
        writer.writeByte(flags);
    }
}
