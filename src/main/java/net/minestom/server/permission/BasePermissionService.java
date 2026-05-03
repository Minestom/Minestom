package net.minestom.server.permission;

import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasePermissionService implements PermissionService {
    @Override
    public void addPermission(@NotNull Permissible handler, @NotNull String permission) {
    }

    @Override
    public void removePermission(@NotNull Permissible handler, @NotNull String permission) {
    }

    @Override
    public @Nullable Boolean getPermission(@NotNull Permissible handler, @NotNull String permission) {
        if (handler instanceof ConsoleSender) {
            return true;
        }
        if (handler instanceof Player player) {
            return player.getPermissionLevel() == 4;
        }
        return null;
    }

    @Override
    public boolean hasPermission(@NotNull Permissible handler, @NotNull String permission) {
        if (handler instanceof ConsoleSender) {
            return true;
        }
        if (handler instanceof Player player) {
            return player.getPermissionLevel() == 4;
        }
        return false;
    }
}
