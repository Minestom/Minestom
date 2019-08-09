package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientTeleportConfirmPacket implements ClientPlayPacket {

    public int teleportId;

    @Override
    public void process(Player player) {

    }

    @Override
    public void read(Buffer buffer) {
        this.teleportId = Utils.readVarInt(buffer);
    }
}
