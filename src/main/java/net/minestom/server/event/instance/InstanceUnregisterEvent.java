package net.minestom.server.event.instance;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an instance is unregistered
 */
public record InstanceUnregisterEvent(@NotNull Instance instance) implements InstanceEvent {}
