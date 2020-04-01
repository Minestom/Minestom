package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

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
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(id -> {
            this.type = id;

            switch (id) {
                case 0:
                    reader.readSizedString((string, length) -> {
                        callback.run();
                    });
                    break;
                case 1:
                    reader.readBoolean(value -> craftingRecipeBookOpen = value);
                    reader.readBoolean(value -> craftingRecipeFilterActive = value);
                    reader.readBoolean(value -> smeltingRecipeBookOpen = value);
                    reader.readBoolean(value -> smeltingRecipeFilterActive = value);
                    reader.readBoolean(value -> blastingRecipeBookOpen = value);
                    reader.readBoolean(value -> blastingRecipeFilterActive = value);
                    reader.readBoolean(value -> smokingRecipeBookOpen = value);
                    reader.readBoolean(value -> {
                        smokingRecipeFilterActive = value;
                        callback.run();
                    });
                    break;
            }

        });
    }
}
