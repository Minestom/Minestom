package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientCraftRecipeRequest(byte windowId, String recipe, boolean makeAll) implements ClientPacket {
    public ClientCraftRecipeRequest {
        if (recipe.length() > 256) {
            throw new IllegalArgumentException("'recipe' cannot be longer than 256 characters.");
        }
    }

    public ClientCraftRecipeRequest(BinaryReader reader) {
        this(reader.readByte(), reader.readSizedString(256), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeSizedString(recipe);
        writer.writeBoolean(makeAll);
    }
}
