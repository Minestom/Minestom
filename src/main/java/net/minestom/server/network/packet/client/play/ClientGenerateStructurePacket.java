package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientGenerateStructurePacket extends ClientPlayPacket {

    public BlockPosition blockPosition;
    public int level;
    public boolean keepJigsaws;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.level = reader.readVarInt();
        this.keepJigsaws = reader.readBoolean();
    }
}
