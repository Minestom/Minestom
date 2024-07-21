package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record CraftRecipeResponse(byte windowId, String recipe) implements ServerPacket.Play {
    public CraftRecipeResponse(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, windowId);
        writer.write(STRING, recipe);
    }

}
