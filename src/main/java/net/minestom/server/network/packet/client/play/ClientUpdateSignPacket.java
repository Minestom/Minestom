package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientUpdateSignPacket(@NotNull Point blockPosition,
                                     @NotNull String line1, @NotNull String line2,
                                     @NotNull String line3, @NotNull String line4) implements ClientPacket {
    public ClientUpdateSignPacket(BinaryReader reader) {
        this(reader.readBlockPosition(),
                reader.readSizedString(384), reader.readSizedString(384),
                reader.readSizedString(384), reader.readSizedString(384));
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
