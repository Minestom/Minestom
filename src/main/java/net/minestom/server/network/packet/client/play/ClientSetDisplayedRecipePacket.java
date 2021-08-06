package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientSetDisplayedRecipePacket extends ClientPlayPacket {

    public String recipeId = "";

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.recipeId = reader.readSizedString(256);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(recipeId);
    }
}