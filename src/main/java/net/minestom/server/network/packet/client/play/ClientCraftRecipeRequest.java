package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientCraftRecipeRequest extends ClientPlayPacket {

    public byte windowId;
    public String recipe;
    public boolean makeAll;

    @Override
    public void read(BinaryReader reader) {
        this.windowId = reader.readByte();
        this.recipe = reader.readSizedString();
        this.makeAll = reader.readBoolean();
    }
}
