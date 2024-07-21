package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ResetScorePacket(@NotNull String owner, @Nullable String objective) implements ServerPacket.Play {

    public ResetScorePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.readOptional(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, owner);
        writer.writeOptional(STRING, objective);
    }

}
