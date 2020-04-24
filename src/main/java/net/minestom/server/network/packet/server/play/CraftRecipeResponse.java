package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class CraftRecipeResponse implements ServerPacket {

    public byte windowId;
    public String recipe;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(windowId);
        writer.writeSizedString(recipe);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CRAFT_RECIPE_RESPONSE;
    }
}
