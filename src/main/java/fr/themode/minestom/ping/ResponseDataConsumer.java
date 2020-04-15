package fr.themode.minestom.ping;

import fr.themode.minestom.net.player.PlayerConnection;

@FunctionalInterface
public interface ResponseDataConsumer {

    void accept(PlayerConnection playerConnection, ResponseData responseData);

}
