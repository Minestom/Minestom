package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientChatMessagePacket implements ClientPlayPacket {

    private String message;

    @Override
    public void process(Player player) {
        Main.getConnectionManager().getOnlinePlayers().forEach(p -> p.sendMessage(String.format("<%s> %s", player.getUsername(), message)));
    }

    @Override
    public void read(Buffer buffer) {
        this.message = Utils.readString(buffer);
    }
}
