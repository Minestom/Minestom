package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PermissionService {
    void addPermission(@NotNull Permissible handler, @NotNull String permission);

    void removePermission(@NotNull Permissible handler, @NotNull String permission);

    @Nullable
    Boolean getPermission(@NotNull Permissible handler, @NotNull String permission);

    boolean hasPermission(@NotNull Permissible handler, @NotNull String permission);
}
