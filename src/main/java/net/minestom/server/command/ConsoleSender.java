package net.minestom.server.command;

import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents the console when sending a command to the server.
 */
public class ConsoleSender implements CommandSender {

    private final Set<Permission> permissions = new CopyOnWriteArraySet<>();

    @Override
    public void sendMessage(@NotNull String message) {
        System.out.println(message);
    }

    @NotNull
    @Override
    public Set<Permission> getAllPermissions() {
        return permissions;
    }
}
