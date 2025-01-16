package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemDropEvent(@NotNull Player player, @NotNull ItemStack itemStack, boolean cancelled) implements PlayerInstanceEvent, ItemEvent, CancellableEvent<ItemDropEvent> {

    public ItemDropEvent(@NotNull Player player, @NotNull ItemStack itemStack) {
        this(player, itemStack, false);
    }

    @Override
    public @NotNull Player player() {
        return player;
    }

    @NotNull
    public ItemStack itemStack() {
        return itemStack;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<ItemDropEvent> {

        public Mutator(@NotNull ItemDropEvent event) {
            super(event);
        }

        @Override
        public @NotNull ItemDropEvent mutated() {
            return new ItemDropEvent(this.event.player, this.event.itemStack, this.isCancelled());
        }
    }
}
