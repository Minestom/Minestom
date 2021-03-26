package net.minestom.server.command;

import net.minestom.server.command.builder.CommandContext;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.command.builder.Arguments;
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
 * Although this class implemented {@link CommandSender} and thus {@link Audience}, no
 * data can be sent to this sender because it's purpose is to process the data of
 * {@link CommandContext#getReturnData()}.
 */
public class ServerSender implements CommandSender {

    private final Set<Permission> permissions = new CopyOnWriteArraySet<>();
    private final Set<PermissionVerifier> permissionVerifiers = new CopyOnWriteArraySet<>();

    public ServerSender() {
        addVerifier(new WildcardPermissionVerifier());
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
