package net.minestom.server.utils.consumer;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PlayerConsumer extends EntityConsumer {
    void accept(@NotNull Player player);
}
