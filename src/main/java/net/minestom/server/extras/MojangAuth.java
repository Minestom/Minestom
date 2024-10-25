package net.minestom.server.extras;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.security.KeyPair;

public final class MojangAuth {
    private static volatile boolean enabled = false;
    private static volatile KeyPair keyPair;

    /**
     * Enables mojang authentication on the server.
     * <p>
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    public static void init() {
        Check.stateCondition(enabled, "Mojang auth is already enabled!");
        Check.stateCondition(MinecraftServer.process().isAlive(), "The server has already been started!");
        Check.stateCondition(VelocityProxy.isEnabled(), "Velocity modern forwarding should not be enabled with MojangAuth");

        MojangAuth.enabled = true;
        // Generate necessary fields...
        MojangAuth.keyPair = MojangCrypt.generateKeyPair();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static @Nullable KeyPair getKeyPair() {
        return keyPair;
    }
}
