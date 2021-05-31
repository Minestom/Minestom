package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SpawnPositionPacket implements ServerPacket {

    public int x, y, z;
    public float angle;

    public SpawnPositionPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(x, y, z);
        writer.writeFloat(angle);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        BlockPosition pos = reader.readBlockPosition();
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
        angle = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_POSITION;
    }
}
