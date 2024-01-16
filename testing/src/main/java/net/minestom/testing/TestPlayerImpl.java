package net.minestom.testing;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestPlayerImpl extends Player {
    public TestPlayerImpl(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);
    }

    @Override
    public void sendChunk(@NotNull Chunk chunk) {
        // Send immediately
        sendPacket(chunk.getFullDataPacket());
    }
}
