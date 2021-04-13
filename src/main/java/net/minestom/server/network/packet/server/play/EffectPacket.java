package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EffectPacket implements ServerPacket {

    public int effectId;
    public BlockPosition position;
    public int data;
    public boolean disableRelativeVolume;

    public EffectPacket() {
        position = new BlockPosition(0,0,0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(effectId);
        writer.writeBlockPosition(position);
        writer.writeInt(data);
        writer.writeBoolean(disableRelativeVolume);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        effectId = reader.readInt();
        position = reader.readBlockPosition();
        data = reader.readInt();
        disableRelativeVolume = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.EFFECT;
    }
}
