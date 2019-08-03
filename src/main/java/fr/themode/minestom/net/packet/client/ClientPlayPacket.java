package fr.themode.minestom.net.packet.client;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPacket;

public interface ClientPlayPacket extends ClientPacket {

    void process(Player player);

}
