package net.minestom.server.extras;

import lombok.Getter;
import net.minestom.server.MinecraftServer;

public class MojangAuth {

    @Getter
    private static boolean usingMojangAuth = false;

    /**
     * Enable mojang authentication on the server.
     */
    public static void init() {
        if (MinecraftServer.getNettyServer().getAddress() == null) {
            usingMojangAuth = true;
        } else {
            throw new IllegalStateException("The server has already been started");
        }
    }
}
