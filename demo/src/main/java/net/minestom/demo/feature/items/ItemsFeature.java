package net.minestom.demo.feature.items;

import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.CreativeInventoryActionEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Item-stack showcase:
 * <ul>
 *   <li>{@code /give} command.</li>
 *   <li>First-spawn gift: stone with {@code CAN_PLACE_ON}/{@code CAN_BREAK}
 *       predicates, a bundle with diamonds and rabbit feet, an iron-nugget
 *       "food" with a {@link Consumable} component, and a purple bed.</li>
 *   <li>{@link PickupItemEvent}: cancel if the player has no inventory room.</li>
 *   <li>{@link ItemDropEvent}: throw the dropped item forward like vanilla.</li>
 *   <li>{@link CreativeInventoryActionEvent}: convert APPLE → GOLDEN_APPLE,
 *       and block taking ENCHANTED_GOLDEN_APPLE.</li>
 *   <li>{@link PlayerFinishItemUseEvent}: react to eating an apple.</li>
 * </ul>
 */
public final class ItemsFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(new GiveCommand());

        process.eventHandler().addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();
            ItemStack stone = ItemStack.builder(Material.STONE)
                    .amount(64)
                    .set(DataComponents.CAN_PLACE_ON, new BlockPredicates(new BlockPredicate(Block.STONE)))
                    .set(DataComponents.CAN_BREAK, new BlockPredicates(new BlockPredicate(Block.DIAMOND_ORE)))
                    .build();
            player.getInventory().addItemStack(stone);

            ItemStack bundle = ItemStack.builder(Material.BUNDLE)
                    .set(DataComponents.BUNDLE_CONTENTS, List.of(
                            ItemStack.of(Material.DIAMOND, 5),
                            ItemStack.of(Material.RABBIT_FOOT, 5)
                    ))
                    .build();
            player.getInventory().addItemStack(bundle);

            PlayerInventory inventory = player.getInventory();
            inventory.addItemStack(food(20));
            inventory.addItemStack(ItemStack.of(Material.PURPLE_BED));
        });

        process.eventHandler().addListener(PickupItemEvent.class, event -> {
            final Entity entity = event.getLivingEntity();
            if (entity instanceof Player player) {
                final ItemStack itemStack = event.getItemEntity().getItemStack();
                event.setCancelled(!player.getInventory().addItemStack(itemStack));
            }
        });

        process.eventHandler().addListener(ItemDropEvent.class, event -> {
            final Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemStack();

            Pos playerPos = player.getPosition();
            ItemEntity itemEntity = new ItemEntity(droppedItem);
            itemEntity.setPickupDelay(Duration.of(500, TimeUnit.MILLISECOND));
            itemEntity.setInstance(player.getInstance(), playerPos.withY(y -> y + 1.5));
            Vec velocity = playerPos.direction().mul(6);
            itemEntity.setVelocity(velocity);
        });

        process.eventHandler().addListener(CreativeInventoryActionEvent.class, event -> {
            if (event.getClickedItem().material() == Material.APPLE) {
                event.setClickedItem(ItemStack.of(Material.GOLDEN_APPLE, event.getClickedItem().amount()));
            } else if (event.getClickedItem().material() == Material.ENCHANTED_GOLDEN_APPLE) {
                event.setCancelled(true);
            }
        });

        process.eventHandler().addListener(PlayerFinishItemUseEvent.class, event -> {
            if (event.getItemStack().material() == Material.APPLE) {
                event.getPlayer().sendMessage("yummy yummy apple");
            }
        });
    }

    /**
     * Iron nugget with a {@link Consumable} component — used as the
     * spawn-gift "food" item.
     */
    private static ItemStack food(int consumeTicks) {
        return ItemStack.builder(Material.IRON_NUGGET)
                .amount(64)
                .set(DataComponents.CONSUMABLE, new Consumable(
                        (float) consumeTicks / 20,
                        ItemAnimation.EAT,
                        SoundEvent.BLOCK_CHAIN_STEP,
                        true,
                        new ArrayList<>()))
                .build();
    }
}
