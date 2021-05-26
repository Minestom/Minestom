package net.minestom.server.extras;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.security.KeyPair;

public final class MojangAuth {

    private static volatile boolean enabled = false;

    private static KeyPair keyPair;

    /**
     * Enables mojang authentication on the server.
     * <p>
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    public static void init() {
        Check.stateCondition(enabled, "Mojang auth is already enabled!");
        Check.stateCondition(MinecraftServer.isStarted(), "The server has already been started!");

        enabled = true;

        // Generate necessary fields...
        keyPair = MojangCrypt.generateKeyPair();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public static KeyPair getKeyPair() {
        return keyPair;
    }
}
