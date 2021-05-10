package net.minestom.server.extras;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.net.Proxy;
import java.security.KeyPair;

public final class MojangAuth {

    private static volatile boolean enabled = false;

    private static KeyPair keyPair;
    private static AuthenticationService authService;
    private static MinecraftSessionService sessionService;

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
        authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        sessionService = authService.createMinecraftSessionService();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public static KeyPair getKeyPair() {
        return keyPair;
    }

    @Nullable
    public static AuthenticationService getAuthService() {
        return authService;
    }

    @Nullable
    public static MinecraftSessionService getSessionService() {
        return sessionService;
    }
}
