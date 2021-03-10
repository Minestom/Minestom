package net.minestom.server.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Sender used in {@link CommandManager#executeServerCommand(String)}.
 * <p>
 * Be aware that {@link #sendMessage(String)} is empty on purpose because the purpose
 * of this sender is to process the data of {@link CommandContext#getReturnData()}.
 */
public class ServerSender implements CommandSender {

    private final Set<Permission> permissions = Collections.unmodifiableSet(new HashSet<>());

    @Override
    public void sendMessage(@NotNull String message) {
        // Empty on purpose
    }

    @NotNull
    @Override
    public Set<Permission> getAllPermissions() {
        return permissions;
    }
}
