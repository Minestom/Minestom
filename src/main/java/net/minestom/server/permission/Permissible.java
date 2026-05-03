package net.minestom.server.permission;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Представляет объект, который может иметь разрешения.
 */
public interface Permissible {
    default void addPermission(@NotNull String permission) {
        MinecraftServer.getPermissionService().addPermission(this, permission);
    }

    default void removePermission(@NotNull String permission) {
        MinecraftServer.getPermissionService().removePermission(this, permission);
    }

    @Nullable
    default Boolean getPermission(@NotNull String permission) {
        return MinecraftServer.getPermissionService().getPermission(this, permission);
    }

    default boolean hasPermission(@NotNull String permission) {
        return MinecraftServer.getPermissionService().hasPermission(this, permission);
    }
}
