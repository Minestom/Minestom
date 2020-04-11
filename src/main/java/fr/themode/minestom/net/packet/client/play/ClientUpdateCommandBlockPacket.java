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
    public void read(PacketReader reader, Runnable callback) {
        reader.readBlockPosition(blockPosition1 -> blockPosition = blockPosition1);
        reader.readSizedString((string, length) -> command = string);
        reader.readVarInt(i -> mode = Mode.values()[i]);
        reader.readByte(value -> {
            flags = value;
            callback.run();
        });
    }

    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }

}
