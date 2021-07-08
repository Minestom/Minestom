package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class SpawnPositionPacket implements ServerPacket {

    public Point position = Vec.ZERO;
    public float angle;

    public SpawnPositionPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(position);
        writer.writeFloat(angle);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.position = reader.readBlockPosition();
        this.angle = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_POSITION;
    }
}
