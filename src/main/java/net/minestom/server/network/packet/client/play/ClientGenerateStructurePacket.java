package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientGenerateStructurePacket extends ClientPlayPacket {

    public BlockPosition blockPosition = new BlockPosition(0,0,0);
    public int level;
    public boolean keepJigsaws;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.level = reader.readVarInt();
        this.keepJigsaws = reader.readBoolean();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(level);
        writer.writeBoolean(keepJigsaws);
    }
}
