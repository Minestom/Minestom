package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

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
