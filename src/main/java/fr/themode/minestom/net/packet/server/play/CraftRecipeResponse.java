package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
