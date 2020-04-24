package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;

public class ClientUpdateCommandBlockPacket extends ClientPlayPacket {

    public BlockPosition blockPosition;
    public String command;
    public Mode mode;
    public byte flags;

    @Override
    public void read(PacketReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.command = reader.readSizedString();
        this.mode = Mode.values()[reader.readVarInt()];
        this.flags = reader.readByte();
    }

    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }

}
