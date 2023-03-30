package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientCraftRecipeRequest(byte windowId, String recipe, boolean makeAll) implements ClientPacket {
    public ClientCraftRecipeRequest {
        if (recipe.length() > 256) {
            throw new IllegalArgumentException("'recipe' cannot be longer than 256 characters.");
        }
    }

    public ClientCraftRecipeRequest(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(STRING), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, windowId);
        writer.write(STRING, recipe);
        writer.write(BOOLEAN, makeAll);
    }
}
