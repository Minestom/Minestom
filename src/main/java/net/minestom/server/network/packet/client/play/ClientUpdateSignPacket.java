package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class ClientUpdateSignPacket extends ClientPlayPacket {

    public Point blockPosition = Vec.ZERO;
    public String line1 = "";
    public String line2 = "";
    public String line3 = "";
    public String line4 = "";

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.line1 = reader.readSizedString(384);
        this.line2 = reader.readSizedString(384);
        this.line3 = reader.readSizedString(384);
        this.line4 = reader.readSizedString(384);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        if (line1.length() > 384)
            throw new IllegalArgumentException("line1 is too long! Signs allow a maximum of 384 characters per line.");
        if (line2.length() > 384)
            throw new IllegalArgumentException("line2 is too long! Signs allow a maximum of 384 characters per line.");
        if (line3.length() > 384)
            throw new IllegalArgumentException("line3 is too long! Signs allow a maximum of 384 characters per line.");
        if (line4.length() > 384)
            throw new IllegalArgumentException("line4 is too long! Signs allow a maximum of 384 characters per line.");
        writer.writeSizedString(line1);
        writer.writeSizedString(line2);
        writer.writeSizedString(line3);
        writer.writeSizedString(line4);
    }
}
