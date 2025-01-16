package net.minestom.server.event.item;

import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

public record PickupExperienceEvent(@NotNull Player player, @NotNull ExperienceOrb experienceOrb, short experienceCount, boolean cancelled) implements PlayerInstanceEvent, CancellableEvent<PickupExperienceEvent> {

    public PickupExperienceEvent(@NotNull Player player, @NotNull ExperienceOrb experienceOrb) {
        this(player, experienceOrb, experienceOrb.getExperienceCount(), false);
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PickupExperienceEvent> {
        private final Player player;
        private final ExperienceOrb experienceOrb;
        private short experienceCount;
        private boolean cancelled;

        public Mutator(PickupExperienceEvent event) {
            this.player = event.player;
            this.experienceOrb = event.experienceOrb;
            this.experienceCount = event.experienceCount;
            this.cancelled = event.cancelled;
        }
        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        public short getExperienceCount() {
            return experienceCount;
        }

        public void setExperienceCount(short experienceCount) {
            this.experienceCount = experienceCount;
        }

        @Override
        public @NotNull PickupExperienceEvent mutated() {
            return new PickupExperienceEvent(this.player, this.experienceOrb, this.experienceCount, this.cancelled);
        }
    }
}
