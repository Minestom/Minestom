package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class UpdateViewPositionPacket implements ServerPacket {

    public int chunkX, chunkZ;

    public UpdateViewPositionPacket(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public UpdateViewPositionPacket() {
        this(0, 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(chunkX);
        writer.writeVarInt(chunkZ);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        chunkX = reader.readVarInt();
        chunkZ = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_VIEW_POSITION;
    }
}
