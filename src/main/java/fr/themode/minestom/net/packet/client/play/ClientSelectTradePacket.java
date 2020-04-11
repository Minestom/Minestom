package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientSelectTradePacket extends ClientPlayPacket {

    public int selectedSlot;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(i -> {
            selectedSlot = i;
            callback.run();
        });
    }
}
