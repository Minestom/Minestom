package fr.themode.minestom.net.packet.client;

import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.player.PlayerConnection;

public interface ClientPreplayPacket extends ClientPacket {


    void process(PlayerConnection connection, ConnectionManager connectionManager);

}
