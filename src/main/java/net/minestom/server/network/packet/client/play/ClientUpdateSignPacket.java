package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ClientUpdateSignPacket(@NotNull Point blockPosition,
                                     @NotNull List<String> lines) implements ClientPacket {
    public ClientUpdateSignPacket {
        lines = List.copyOf(lines);
        if (lines.size() != 4) {
            throw new IllegalArgumentException("Signs must have 4 lines!");
        }
        for (String line : lines) {
            if (line.length() > 384) {
                throw new IllegalArgumentException("Signs must have a maximum of 384 characters per line!");
            }
        }
    }

    public ClientUpdateSignPacket(BinaryReader reader) {
        this(reader.readBlockPosition(), readLines(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeSizedString(lines.get(0));
        writer.writeSizedString(lines.get(1));
        writer.writeSizedString(lines.get(2));
        writer.writeSizedString(lines.get(3));
    }

    private static List<String> readLines(BinaryReader reader) {
        return List.of(reader.readSizedString(384), reader.readSizedString(384),
                reader.readSizedString(384), reader.readSizedString(384));
    }
}
