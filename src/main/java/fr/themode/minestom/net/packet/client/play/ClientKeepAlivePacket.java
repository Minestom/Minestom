package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.net.packet.server.play.DisconnectPacket;

public class ClientKeepAlivePacket implements ClientPlayPacket {

    private long id;

    @Override
    public void process(Player player) {
        if (id != player.getLastKeepAlive()) {
            player.getPlayerConnection().sendPacket(new DisconnectPacket("{\"text\": \"Bad Keep Alive packet\"}"));
            player.getPlayerConnection().getConnection().close();
        }
    }

    @Override
    public void read(Buffer buffer) {
        this.id = buffer.getLong();
    }
}
