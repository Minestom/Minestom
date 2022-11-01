package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record UpdateScorePacket(@NotNull String entityName, byte action,
                                @NotNull String objectiveName, int value) implements ServerPacket {
    public UpdateScorePacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private UpdateScorePacket(UpdateScorePacket packet) {
        this(packet.entityName, packet.action, packet.objectiveName, packet.value);
    }

    private static UpdateScorePacket read(@NotNull NetworkBuffer reader) {
        var entityName = reader.read(STRING);
        var action = reader.read(BYTE);
        var objectiveName = reader.read(STRING);
        var value = action != 1 ? reader.read(VAR_INT) : 0;
        return new UpdateScorePacket(entityName, action, objectiveName, value);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, entityName);
        writer.write(BYTE, action);
        writer.write(STRING, objectiveName);
        if (action != 1) writer.write(VAR_INT, value);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_SCORE;
    }
}
