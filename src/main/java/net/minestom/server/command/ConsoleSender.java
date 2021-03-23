package net.minestom.server.command;

import net.minestom.server.permission.Permission;
import net.minestom.server.permission.verifier.AllPermissionVerifier;
import net.minestom.server.permission.verifier.PermissionVerifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents the console when sending a command to the server.
 */
public class ConsoleSender implements CommandSender {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsoleSender.class);

    private final Set<Permission> permissions = new CopyOnWriteArraySet<>();
    private final Set<PermissionVerifier> permissionVerifiers = new CopyOnWriteArraySet<>();

    public ConsoleSender() {
        addVerifier(new AllPermissionVerifier());
    }

    @Override
    public void sendMessage(@NotNull String message) {
        LOGGER.info(message);
    }

    @NotNull
    @Override
    public Set<Permission> getAllPermissions() {
        return permissions;
    }

    @NotNull
    @Override
    public Set<PermissionVerifier> getVerifiers() {
        return permissionVerifiers;
    }
}
