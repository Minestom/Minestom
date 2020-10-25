package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SpawnPositionPacket implements ServerPacket {

    public int x, y, z;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(x, y, z);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_POSITION;
    }
}
