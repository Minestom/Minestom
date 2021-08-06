package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientCraftRecipeRequest extends ClientPlayPacket {

    public byte windowId;
    public String recipe = "";
    public boolean makeAll;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.windowId = reader.readByte();
        this.recipe = reader.readSizedString(256);
        this.makeAll = reader.readBoolean();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        if(recipe.length() > 256) {
            throw new IllegalArgumentException("'recipe' cannot be longer than 256 characters.");
        }
        writer.writeSizedString(recipe);
        writer.writeBoolean(makeAll);
    }
}
