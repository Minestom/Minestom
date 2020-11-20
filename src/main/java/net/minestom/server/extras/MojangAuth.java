package net.minestom.server.extras;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.mojangAuth.MojangCrypt;

import java.net.Proxy;
import java.security.KeyPair;

public final class MojangAuth {

    private static boolean enabled = false;

    private static final KeyPair keyPair = MojangCrypt.generateKeyPair();
    private static final AuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
    private static final MinecraftSessionService sessionService = authService.createMinecraftSessionService();

    /**
     * Enables mojang authentication on the server.
     * <p>
     * Be aware that enabling a proxy will make Mojang authentication ignored.
     */
    public static void init() {
        if (MinecraftServer.getNettyServer().getAddress() == null) {
            enabled = true;
        } else {
            throw new IllegalStateException("The server has already been started");
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static KeyPair getKeyPair() {
        return keyPair;
    }

    public static AuthenticationService getAuthService() {
        return authService;
    }

    public static MinecraftSessionService getSessionService() {
        return sessionService;
    }
}
