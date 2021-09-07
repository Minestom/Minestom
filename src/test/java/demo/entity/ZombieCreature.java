package demo.entity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryHolder;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeathCombatEventPacket;

public class ZombieCreature extends EntityCreature implements InventoryHolder<Inventory> {

    private final Inventory inventory;

    public ZombieCreature() {
        super(EntityType.ZOMBIE);
        addAIGroup(
                new EntityAIGroupBuilder()
                        .addGoalSelector(new RandomLookAroundGoal(this, 20))
                        .build()
        );
        inventory = new Inventory(InventoryType.CHEST_3_ROW, "Zombie");
    }

    @Override
    public void kill() {
        if (!isDead()) {
            final ItemStack[] stacks = inventory.getItemStacks();
            for(int i = 0; i < stacks.length; i++) {
                new ItemEntity(stacks[i], position).setInstance(instance);
            }

            instance.playSound(Sound.sound(Key.key("minecraft", "entity.zombie.death"), Sound.Source.HOSTILE, 0.5F, 0.5F));
        }
        super.kill();
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
