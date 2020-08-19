package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

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
    public void write(BinaryWriter writer) {
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
