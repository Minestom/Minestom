package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
