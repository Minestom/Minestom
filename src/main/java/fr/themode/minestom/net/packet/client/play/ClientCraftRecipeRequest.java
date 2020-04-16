package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientCraftRecipeRequest extends ClientPlayPacket {

    public byte windowId;
    public String recipe;
    public boolean makeAll;

    @Override
    public void read(PacketReader reader) {
        this.windowId = reader.readByte();
        this.recipe = reader.readSizedString();
        this.makeAll = reader.readBoolean();
    }
}
