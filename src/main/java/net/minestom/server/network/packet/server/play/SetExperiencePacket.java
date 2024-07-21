package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetExperiencePacket(float percentage, int level, int totalExperience) implements ServerPacket.Play {
    public SetExperiencePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(FLOAT), reader.read(VAR_INT), reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(FLOAT, percentage);
        writer.write(VAR_INT, level);
        writer.write(VAR_INT, totalExperience);
    }

}
