package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class UpdateViewDistancePacket implements ServerPacket {

    public int viewDistance;

    public UpdateViewDistancePacket(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    public UpdateViewDistancePacket() {
        this(0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(viewDistance);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        viewDistance = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_VIEW_DISTANCE;
    }
}
