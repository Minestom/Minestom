package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.BlockPosition;

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
