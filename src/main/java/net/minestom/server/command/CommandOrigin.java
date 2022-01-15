package net.minestom.server.command;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CommandOrigin(@NotNull CommandSender sender, @Nullable Entity entity, @Nullable Instance instance, @Nullable Pos position) {

    public CommandOrigin(@NotNull CommandSender sender, @Nullable Entity entity, @Nullable Instance instance) {
        this(sender, entity, instance, null);
    }

    public CommandOrigin(@NotNull CommandSender sender, @Nullable Entity entity) {
        this(sender, entity, null, null);
    }

    public CommandOrigin(@NotNull CommandSender sender) {
        this(sender, null, null, null);
    }

    public @NotNull CommandOrigin withSender(@NotNull CommandSender sender) {
        return new CommandOrigin(sender, entity, instance, position);
    }

    public @NotNull CommandOrigin withEntity(@Nullable Entity entity) {
        return new CommandOrigin(sender, entity, instance, position);
    }

    public @NotNull CommandOrigin withInstance(@Nullable Instance instance) {
        return new CommandOrigin(sender, entity, instance, position);
    }

    public @NotNull CommandOrigin withPosition(@Nullable Pos position) {
        return new CommandOrigin(sender, entity, instance, position);
    }

    public static @NotNull CommandOrigin ofPlayer(@NotNull Player player) {
        return new CommandOrigin(player, player, player.getInstance(), player.getPosition());
    }

}

