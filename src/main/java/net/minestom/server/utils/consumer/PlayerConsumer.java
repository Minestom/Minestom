package net.minestom.server.utils.consumer;

import net.minestom.server.entity.Player;

public interface PlayerConsumer extends EntityConsumer {
    void accept(Player player);
}
