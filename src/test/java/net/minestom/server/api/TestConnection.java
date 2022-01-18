package net.minestom.server.api;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.concurrent.CompletableFuture;

public interface TestConnection {
    CompletableFuture<Player> connect(Instance instance);
}
