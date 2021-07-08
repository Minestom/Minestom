package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class ClientUpdateCommandBlockPacket extends ClientPlayPacket {

    public Point blockPosition;
    public String command;
    public Mode mode;
    public byte flags;

    public ClientUpdateCommandBlockPacket() {
        blockPosition = Vec.ZERO;
        command = "";
        mode = Mode.REDSTONE;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.command = reader.readSizedString(Short.MAX_VALUE);
        this.mode = Mode.values()[reader.readVarInt()];
        this.flags = reader.readByte();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeSizedString(command);
        writer.writeVarInt(mode.ordinal());
        writer.writeByte(flags);
    }

    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }

}
