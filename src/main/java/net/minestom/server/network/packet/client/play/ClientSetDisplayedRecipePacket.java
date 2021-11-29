package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientSetDisplayedRecipePacket(@NotNull String recipeId) implements ClientPacket {
    public ClientSetDisplayedRecipePacket(BinaryReader reader) {
        this(reader.readSizedString(256));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(recipeId);
    }
}