package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientRecipeBookData extends ClientPlayPacket {

    public int type;

    public String recipeId = "";

    public boolean craftingRecipeBookOpen;
    public boolean craftingRecipeFilterActive;
    public boolean smeltingRecipeBookOpen;
    public boolean smeltingRecipeFilterActive;
    public boolean blastingRecipeBookOpen;
    public boolean blastingRecipeFilterActive;
    public boolean smokingRecipeBookOpen;
    public boolean smokingRecipeFilterActive;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.type = reader.readVarInt();

        switch (type) {
            case 0:
                this.recipeId = reader.readSizedString(256);
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

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(type);

        switch (type) {
            case 0:
                if(recipeId.length() > 256)
                    throw new IllegalArgumentException("recipeId must be less than 256 bytes");
                writer.writeSizedString(recipeId);
                break;

            case 1:
                writer.writeBoolean(this.craftingRecipeBookOpen);
                writer.writeBoolean(this.craftingRecipeFilterActive);
                writer.writeBoolean(this.smeltingRecipeBookOpen);
                writer.writeBoolean(this.smeltingRecipeFilterActive);
                writer.writeBoolean(this.blastingRecipeBookOpen);
                writer.writeBoolean(this.blastingRecipeFilterActive);
                writer.writeBoolean(this.smokingRecipeBookOpen);
                writer.writeBoolean(this.smokingRecipeFilterActive);
                break;
        }
    }
}
