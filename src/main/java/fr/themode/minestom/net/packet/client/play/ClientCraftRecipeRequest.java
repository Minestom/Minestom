package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientCraftRecipeRequest extends ClientPlayPacket {

    public byte windowId;
    public String recipe;
    public boolean makeAll;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readByte(value -> windowId = value);
        reader.readSizedString((string, length) -> recipe = string);
        reader.readBoolean(value -> {
            makeAll = value;
            callback.run();
        });
    }
}
