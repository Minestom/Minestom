package net.minestom.server.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.verifier.PermissionVerifier;
import net.minestom.server.permission.verifier.WildcardPermissionVerifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Sender used in {@link CommandManager#executeServerCommand(String)}.
 * <p>
 * Be aware that {@link #sendMessage(String)} is empty on purpose because the purpose
 * of this sender is to process the data of {@link CommandContext#getReturnData()}.
 *
 * TODO replace with command executor option?
 */
public class ServerSender implements CommandSender {

    private final Set<Permission> permissions = new CopyOnWriteArraySet<>();
    private final Set<PermissionVerifier> permissionVerifiers = new CopyOnWriteArraySet<>();

    public ServerSender() {
        addVerifier(new WildcardPermissionVerifier());
    }

    @Override
    public void sendMessage(@NotNull String message) {
        // Empty on purpose
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
