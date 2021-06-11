package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class CraftRecipeResponse implements ServerPacket {

    public byte windowId;
    public String recipe;

    public CraftRecipeResponse() {
        recipe = "";
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeSizedString(recipe);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readByte();
        recipe = reader.readSizedString();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CRAFT_RECIPE_RESPONSE;
    }
}
