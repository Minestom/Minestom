package net.minestom.scratch.entity;

import net.minestom.server.entity.EntityStatuses;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.Food;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import net.minestom.server.potion.CustomPotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class HealthHandler {
    private final int entityId;
    private final Consumer<ServerPacket.Play> selfConsumer;
    private final Consumer<ServerPacket.Play> localBroadcastConsumer;

    private float health = 19;
    private int hunger = 10;
    private float saturation = 5;

    private long startEating;
    private int eatingSlot;
    private ItemStack eatingItem;

    public HealthHandler(int entityId, Consumer<ServerPacket.Play> selfConsumer, Consumer<ServerPacket.Play> localBroadcastConsumer) {
        this.entityId = entityId;
        this.selfConsumer = selfConsumer;
        this.localBroadcastConsumer = localBroadcastConsumer;

        resetEatingValues();
    }

    public UpdateHealthPacket healthPacket() {
        return new UpdateHealthPacket(health, hunger, saturation);
    }

    public void startEating(int slot, ItemStack itemStack) {
        if (!itemStack.has(ItemComponent.FOOD)) return;
        this.startEating = System.currentTimeMillis();
        this.eatingSlot = slot;
        this.eatingItem = itemStack;
    }

    public void cancelEating() {
        resetEatingValues();
    }

    public List<Action> updateEating() {
        final long startEating = this.startEating;
        if (startEating == -1) return List.of();
        Food food = eatingItem.get(ItemComponent.FOOD);
        if (food == null) {
            resetEatingValues();
            return List.of();
        }
        final int eatMs = (int) (food.eatSeconds() * 1000);
        if (System.currentTimeMillis() - startEating >= eatMs) {
            List<Action> actions = new ArrayList<>();

            this.hunger = Math.min(20, hunger + food.nutrition());
            this.saturation = Math.min(5, saturation + food.saturationModifier());

            actions.add(new Action.ConsumeItem(eatingSlot));
            for (Food.EffectChance effectChance : food.effects()) {
                if (Math.random() < effectChance.probability()) {
                    actions.add(new Action.ApplyEffect(effectChance.effect()));
                }
            }

            selfConsumer.accept(new EntityStatusPacket(entityId, (byte) EntityStatuses.Player.MARK_ITEM_FINISHED));
            selfConsumer.accept(healthPacket());

            resetEatingValues();

            return List.copyOf(actions);
        }
        return List.of();
    }

    private void resetEatingValues() {
        this.startEating = -1;
        this.eatingSlot = -1;
        this.eatingItem = ItemStack.AIR;
    }

    public sealed interface Action {
        record ConsumeItem(int slot) implements Action {
        }

        record ApplyEffect(CustomPotionEffect potionEffect) implements Action {
        }
    }
}
