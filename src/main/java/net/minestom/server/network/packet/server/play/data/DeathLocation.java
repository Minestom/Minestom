package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public record DeathLocation(@NotNull String dimension, @NotNull Point position) {

}