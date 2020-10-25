package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class CraftRecipeResponse implements ServerPacket {

    public byte windowId;
    public String recipe;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeSizedString(recipe);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CRAFT_RECIPE_RESPONSE;
    }
}
