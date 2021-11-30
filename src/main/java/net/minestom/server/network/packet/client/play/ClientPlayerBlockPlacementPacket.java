package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientPlayerBlockPlacementPacket(@NotNull Player.Hand hand, @NotNull Point blockPosition,
                                               @NotNull BlockFace blockFace,
                                               float cursorPositionX, float cursorPositionY, float cursorPositionZ,
                                               boolean insideBlock) implements ClientPacket {
    public ClientPlayerBlockPlacementPacket(BinaryReader reader) {
        this(Player.Hand.values()[reader.readVarInt()], reader.readBlockPosition(),
                BlockFace.values()[reader.readVarInt()],
                reader.readFloat(), reader.readFloat(), reader.readFloat(),
                reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(hand.ordinal());
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockFace.ordinal());
        writer.writeFloat(cursorPositionX);
        writer.writeFloat(cursorPositionY);
        writer.writeFloat(cursorPositionZ);
        writer.writeBoolean(insideBlock);
    }
}
