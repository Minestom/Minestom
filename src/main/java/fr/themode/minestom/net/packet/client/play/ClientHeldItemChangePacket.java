package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientHeldItemChangePacket implements ClientPlayPacket {

    public short slot;

    @Override
    public void process(Player player) {
        if (slot < 0 || slot > 8)
            return;
        player.refreshHeldSlot(slot);
    }

    @Override
    public void read(Buffer buffer) {
        buffer.getShort();
    }
}
