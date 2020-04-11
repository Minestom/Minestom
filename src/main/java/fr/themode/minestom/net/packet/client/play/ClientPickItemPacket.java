package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPickItemPacket extends ClientPlayPacket {

    public int slotToUse;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(i -> {
            slotToUse = i;
            callback.run();
        });
    }
}
