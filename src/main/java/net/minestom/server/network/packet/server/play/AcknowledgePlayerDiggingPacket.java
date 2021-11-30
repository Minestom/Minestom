package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record AcknowledgePlayerDiggingPacket(@NotNull Point blockPosition, int blockStateId,
                                             @NotNull ClientPlayerDiggingPacket.Status status,
                                             boolean successful) implements ServerPacket {
    public AcknowledgePlayerDiggingPacket(@NotNull Point blockPosition, Block block,
                                          @NotNull ClientPlayerDiggingPacket.Status status, boolean successful) {
        this(blockPosition, block.stateId(), status, successful);
    }

    public AcknowledgePlayerDiggingPacket(BinaryReader reader) {
        this(reader.readBlockPosition(), reader.readVarInt(),
                ClientPlayerDiggingPacket.Status.values()[reader.readVarInt()], reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockStateId);
        writer.writeVarInt(status.ordinal());
        writer.writeBoolean(successful);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACKNOWLEDGE_PLAYER_DIGGING;
    }
}
