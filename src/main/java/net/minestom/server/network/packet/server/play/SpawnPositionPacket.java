package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public class SpawnPositionPacket implements ServerPacket {

    public int x, y, z;
    public float angle;

    public SpawnPositionPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(x, y, z);
        writer.writeFloat(angle);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        Point pos = reader.readBlockPosition();
        this.x = (int) pos.x();
        this.y = (int) pos.y();
        this.z = (int) pos.z();
        this.angle = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_POSITION;
    }
}
