package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public record EntityEquipEvent(@NotNull Entity entity, @NotNull ItemStack equippedItem, @NotNull EquipmentSlot slot) implements EntityInstanceEvent, ItemEvent, MutableEvent<EntityEquipEvent> {

    /**
     * Same as {@link #equippedItem()}.
     */
    @Override
    public @NotNull ItemStack itemStack() {
        return equippedItem;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<EntityEquipEvent> {
        private final Entity entity;
        private ItemStack equippedItem;
        private final EquipmentSlot slot;

        public Mutator(EntityEquipEvent event) {
            this.entity = event.entity;
            this.equippedItem = event.equippedItem;
            this.slot = event.slot;
        }


        /**
         * Same as {@link #getEquippedItem()}.
         */
        public @NotNull ItemStack getItemStack() {
            return getEquippedItem();
        }

        /**
         * Same as {@link #setEquippedItem(ItemStack)}.
         */
        public void setItemStack(@NotNull ItemStack equippedItem) {
            setEquippedItem(equippedItem);
        }

        public @NotNull ItemStack getEquippedItem() {
            return equippedItem;
        }

        public void setEquippedItem(@NotNull ItemStack armorItem) {
            this.equippedItem = armorItem;
        }

        @Override
        public @NotNull EntityEquipEvent mutated() {
            return new EntityEquipEvent(this.entity, this.equippedItem, this.slot);
        }
    }
}
