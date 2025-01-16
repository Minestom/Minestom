package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

public record PlayerStartFlyingWithElytraEvent(@NotNull Player player) implements PlayerInstanceEvent {}