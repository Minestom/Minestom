package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.List;

public class UpdateLightPacket implements ServerPacket {

    public int chunkX;
    public int chunkZ;
    //todo make changeable
    public boolean trustEdges = true;

    public int skyLightMask;
    public int blockLightMask;

    public int emptySkyLightMask;
    public int emptyBlockLightMask;

    public List<byte[]> skyLight;
    public List<byte[]> blockLight;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(chunkX);
        writer.writeVarInt(chunkZ);

        writer.writeBoolean(trustEdges);

        writer.writeVarInt(skyLightMask);
        writer.writeVarInt(blockLightMask);

        writer.writeVarInt(emptySkyLightMask);
        writer.writeVarInt(emptyBlockLightMask);

        //writer.writeVarInt(skyLight.size());
        for (byte[] bytes : skyLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }

        //writer.writeVarInt(blockLight.size());
        for (byte[] bytes : blockLight) {
            writer.writeVarInt(2048); // Always 2048 length
            writer.writeBytes(bytes);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_LIGHT;
    }
}
