package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientAnimationPacket extends ClientPlayPacket {

    public Player.Hand hand;

    @Override
    public void read(PacketReader reader) {
        this.hand = Player.Hand.values()[reader.readVarInt()];
    }
}
