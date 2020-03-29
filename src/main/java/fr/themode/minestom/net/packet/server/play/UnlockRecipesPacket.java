package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class UnlockRecipesPacket implements ServerPacket {

    public int mode;

    public boolean craftingRecipeBookOpen;
    public boolean craftingRecipeBookFilterActive;
    public boolean smeltingRecipeBookOpen;
    public boolean smeltingRecipeBookFilterActive;

    public String[] recipesId;

    // Only if mode = 0
    public String[] initRecipesId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(mode);

        writer.writeBoolean(craftingRecipeBookOpen);
        writer.writeBoolean(craftingRecipeBookFilterActive);
        writer.writeBoolean(smeltingRecipeBookOpen);
        writer.writeBoolean(smeltingRecipeBookFilterActive);

        writer.writeVarInt(recipesId.length);
        for (String string : recipesId) {
            writer.writeSizedString(string);
        }

        if (mode == 0) {
            writer.writeVarInt(initRecipesId.length);
            for (String string : initRecipesId) {
                writer.writeSizedString(string);
            }
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UNLOCK_RECIPES;
    }
}
