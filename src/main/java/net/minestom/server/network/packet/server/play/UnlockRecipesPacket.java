package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class UnlockRecipesPacket implements ServerPacket {

    public int mode;

    public boolean craftingRecipeBookOpen;
    public boolean craftingRecipeBookFilterActive;
    public boolean smeltingRecipeBookOpen;
    public boolean smeltingRecipeBookFilterActive;

    public String[] recipesId;

    // Only if mode = 0
    public String[] initRecipesId;

    /**
     * Default constructor, required for reflection operations.
     */
    public UnlockRecipesPacket() {
        recipesId = new String[0];
        initRecipesId = new String[0];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
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
    public void read(@NotNull BinaryReader reader) {
        mode = reader.readVarInt();

        craftingRecipeBookOpen = reader.readBoolean();
        craftingRecipeBookFilterActive = reader.readBoolean();
        smeltingRecipeBookOpen = reader.readBoolean();
        smeltingRecipeBookFilterActive = reader.readBoolean();

        int length = reader.readVarInt();
        recipesId = new String[length];
        for (int i = 0; i < length; i++) {
            recipesId[i] = reader.readSizedString();
        }

        if (mode == 0) {
            int initRecipesLength = reader.readVarInt();
            initRecipesId = new String[initRecipesLength];
            for (int i = 0; i < length; i++) {
                initRecipesId[i] = reader.readSizedString();
            }
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UNLOCK_RECIPES;
    }
}
