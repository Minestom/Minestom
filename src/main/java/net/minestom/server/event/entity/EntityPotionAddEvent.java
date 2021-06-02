package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

public class EntityPotionAddEvent extends Event implements EntityEvent {

    private final Entity entity;
    private final Potion potion;

    public EntityPotionAddEvent(@NotNull Entity entity, @NotNull Potion potion) {
        this.entity = entity;
        this.potion = potion;
    }

    @NotNull
    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * Returns the potion that was added.
     *
     * @return the added potion.
     */
    @NotNull
    public Potion getPotion() {
        return potion;
    }
}
