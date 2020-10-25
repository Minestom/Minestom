package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientUpdateCommandBlockPacket extends ClientPlayPacket {

    public BlockPosition blockPosition;
    public String command;
    public Mode mode;
    public byte flags;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.command = reader.readSizedString();
        this.mode = Mode.values()[reader.readVarInt()];
        this.flags = reader.readByte();
    }

    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }

}
