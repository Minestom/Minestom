package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientAnimationPacket extends ClientPlayPacket {

    public Player.Hand hand;

    @Override
    public void read(BinaryReader reader) {
        this.hand = Player.Hand.values()[reader.readVarInt()];
    }
}
