package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class UpdateLightPacket implements ServerPacket {

    public int chunkX;
    public int chunkZ;

    public int skyLightMask;
    public int blockLightMask;

    public int emptySkyLightMask;
    public int emptyBlockLightMask;

    public byte[] skyLight;
    public byte[] blockLight;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(chunkX);
        writer.writeVarInt(chunkZ);

        writer.writeVarInt(skyLightMask);
        writer.writeVarInt(blockLightMask);

        writer.writeVarInt(emptySkyLightMask);
        writer.writeVarInt(emptyBlockLightMask);

        writer.writeVarInt(2048); // Always 2048 length
        writer.writeBytes(skyLight);
        writer.writeVarInt(2048); // Always 2048 length
        writer.writeBytes(blockLight);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_LIGHT;
    }
}
