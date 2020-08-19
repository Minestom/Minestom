package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientRecipeBookData extends ClientPlayPacket {

    public int type;

    public String recipeId;

    public boolean craftingRecipeBookOpen;
    public boolean craftingRecipeFilterActive;
    public boolean smeltingRecipeBookOpen;
    public boolean smeltingRecipeFilterActive;
    public boolean blastingRecipeBookOpen;
    public boolean blastingRecipeFilterActive;
    public boolean smokingRecipeBookOpen;
    public boolean smokingRecipeFilterActive;

    @Override
    public void read(BinaryReader reader) {
        this.type = reader.readVarInt();

        switch (type) {
            case 0:
                this.recipeId = reader.readSizedString();
                break;
            case 1:
                this.craftingRecipeBookOpen = reader.readBoolean();
                this.craftingRecipeFilterActive = reader.readBoolean();
                this.smeltingRecipeBookOpen = reader.readBoolean();
                this.smeltingRecipeFilterActive = reader.readBoolean();
                this.blastingRecipeBookOpen = reader.readBoolean();
                this.blastingRecipeFilterActive = reader.readBoolean();
                this.smokingRecipeBookOpen = reader.readBoolean();
                this.smokingRecipeFilterActive = reader.readBoolean();
                break;
        }
    }
}
