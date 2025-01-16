package net.minestom.demo;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.FeatureFlag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.Notification;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.*;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.instance.block.predicate.BlockTypeFilter;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.packet.server.common.CustomReportDetailsPacket;
import net.minestom.server.network.packet.server.common.ServerLinksPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerInit {

    private final Inventory inventory;

    private final EventNode<Event> DEMO_NODE = EventNode.all("demo")
            .addListener(EntityAttackEvent.class, event -> {
                final Entity source = event.entity();
                final Entity entity = event.target();

                entity.takeKnockback(0.4f, Math.sin(source.getPosition().yaw() * 0.017453292), -Math.cos(source.getPosition().yaw() * 0.017453292));

                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    target.damage(Damage.fromEntity(source, 5));
                }

                if (source instanceof Player) {
                    ((Player) source).sendMessage("You attacked something!");
                }
            })
            .addListener(PlayerDeathEvent.class, event -> {
                var mutator = event.mutator();
                mutator.setChatMessage(Component.text("custom death message"));
                return mutator;
            })
            .addListener(PickupItemEvent.class, event -> {
                final Entity entity = event.livingEntity();
                if (entity instanceof Player) {

                    // Cancel event if player does not have enough inventory space
                    final ItemStack itemStack = event.itemStack();
                    if (!((Player) entity).getInventory().addItemStack(itemStack)) {
                        var mutator = event.mutator();
                        mutator.setCancelled(true);
                        return mutator;
                    }
                }

                return null;
            })
            .addListener(ItemDropEvent.class, event -> {
                final Player player = event.player();
                ItemStack droppedItem = event.itemStack();

                Pos playerPos = player.getPosition();
                ItemEntity itemEntity = new ItemEntity(droppedItem);
                itemEntity.setPickupDelay(Duration.of(500, TimeUnit.MILLISECOND));
                itemEntity.setInstance(player.getInstance(), playerPos.withY(y -> y + 1.5));
                Vec velocity = playerPos.direction().mul(6);
                itemEntity.setVelocity(velocity);
            })
            .addListener(PlayerDisconnectEvent.class, event -> System.out.println("DISCONNECTION " + event.player().getUsername()))
            .addListener(AsyncPlayerConfigurationEvent.class, event -> {
                final Player player = event.player();
                final var mutator = event.mutator();
                // Show off adding and removing feature flags
                mutator.removeFeatureFlag(FeatureFlag.TRADE_REBALANCE); // not enabled by default, just removed for demonstration

                var instances = MinecraftServer.getInstanceManager().getInstances();
                Instance instance = instances.stream().skip(new Random().nextInt(instances.size())).findFirst().orElse(null);
                mutator.setSpawningInstance(instance);
                System.out.println("CONFIGURATION " + player.getUsername() + " " + event.firstConfig() + " " + instance);
                int x = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
                int z = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
                player.setRespawnPoint(new Pos(0, 40f, 0));

                System.out.println(instance);
                return mutator;
            })
            .addListener(PlayerSpawnEvent.class, event -> {
                final Player player = event.player();
                player.setGameMode(GameMode.CREATIVE);
                player.setPermissionLevel(4);
                ItemStack itemStack = ItemStack.builder(Material.STONE)
                        .amount(64)
                        .set(ItemComponent.CAN_PLACE_ON, new BlockPredicates(new BlockPredicate(new BlockTypeFilter.Blocks(Block.STONE), null, null)))
                        .set(ItemComponent.CAN_BREAK, new BlockPredicates(new BlockPredicate(new BlockTypeFilter.Blocks(Block.DIAMOND_ORE), null, null)))
                        .build();
                player.getInventory().addItemStack(itemStack);

                player.sendPacket(new CustomReportDetailsPacket(Map.of(
                        "hello", "world"
                )));

                player.sendPacket(new ServerLinksPacket(
                        new ServerLinksPacket.Entry(ServerLinksPacket.KnownLinkType.NEWS, "https://minestom.net"),
                        new ServerLinksPacket.Entry(ServerLinksPacket.KnownLinkType.BUG_REPORT, "https://minestom.net"),
                        new ServerLinksPacket.Entry(Component.text("Hello world!"), "https://minestom.net")
                ));

                // TODO(1.21.2): Handle bundle slot selection
                ItemStack bundle = ItemStack.builder(Material.BUNDLE)
                        .set(ItemComponent.BUNDLE_CONTENTS, List.of(
                                ItemStack.of(Material.DIAMOND, 5),
                                ItemStack.of(Material.RABBIT_FOOT, 5)
                        ))
                        .build();
                player.getInventory().addItemStack(bundle);

                PlayerInventory inventory = event.player().getInventory();
                inventory.addItemStack(getFoodItem(20));
                inventory.addItemStack(getFoodItem(10000));
                inventory.addItemStack(getFoodItem(Integer.MAX_VALUE));

                inventory.addItemStack(ItemStack.of(Material.GOAT_HORN).with(ItemComponent.INSTRUMENT, Instrument.ADMIRE_GOAT_HORN));

                if (event.firstSpawn()) {
                    event.player().sendNotification(new Notification(
                            Component.text("Welcome!"),
                            FrameType.TASK,
                            Material.IRON_SWORD
                    ));

                    player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 0.5f, 1f));

                    player.sendMessage(Component.text("Hello shadow").shadowColor(new AlphaColor(0xFFFF0000)));
                }
            })
            .addListener(PlayerPacketOutEvent.class, event -> {
                //System.out.println("out " + event.getPacket().getClass().getSimpleName());
            })
            .addListener(PlayerPacketEvent.class, event -> {

                //System.out.println("in " + event.getPacket().getClass().getSimpleName());
            })
            .addListener(PlayerUseItemOnBlockEvent.class, event -> {
                if (event.hand() != PlayerHand.MAIN) return;

                var itemStack = event.itemStack();
                var block = event.instance().getBlock(event.position());

                if ("false".equals(block.getProperty("waterlogged")) && itemStack.material().equals(Material.WATER_BUCKET)) {
                    block = block.withProperty("waterlogged", "true");
                } else if ("true".equals(block.getProperty("waterlogged")) && itemStack.material().equals(Material.BUCKET)) {
                    block = block.withProperty("waterlogged", "false");
                } else return;

                event.instance().setBlock(event.position(), block);

            })
            .addListener(PlayerBeginItemUseEvent.class, event -> {
                final Player player = event.player();
                final ItemStack itemStack = event.itemStack();
                final boolean hasProjectile = !itemStack.get(ItemComponent.CHARGED_PROJECTILES, List.of()).isEmpty();
                if (itemStack.material() == Material.CROSSBOW && hasProjectile) {
                    // "shoot" the arrow
                    var mutator = event.mutator();
                    player.setItemInHand(event.hand(), itemStack.without(ItemComponent.CHARGED_PROJECTILES));
                    event.player().sendMessage("pew pew!");
                    mutator.setItemUseDuration(0); // Do not start using the item
                    return mutator;
                }
                return null;
            })
            .addListener(PlayerFinishItemUseEvent.class, event -> {
                if (event.itemStack().material() == Material.APPLE) {
                    event.player().sendMessage("yummy yummy apple");
                }
            })
            .addListener(PlayerCancelItemUseEvent.class, event -> {
                final Player player = event.player();
                final ItemStack itemStack = event.itemStack();
                if (itemStack.material() == Material.CROSSBOW && event.getUseDuration() > 25) {
                    player.setItemInHand(event.getHand(), itemStack.with(ItemComponent.CHARGED_PROJECTILES, List.of(ItemStack.of(Material.ARROW))));
                    return;
                }
            })
            .addListener(PlayerBlockInteractEvent.class, event -> {
                var block = event.block();
                var rawOpenProp = block.getProperty("open");
                if (rawOpenProp != null) {
                    block = block.withProperty("open", String.valueOf(!Boolean.parseBoolean(rawOpenProp)));
                    event.instance().setBlock(event.blockPosition(), block);
                }

                if (block.id() == Block.CRAFTING_TABLE.id()) {
                    event.player().openInventory(new Inventory(InventoryType.CRAFTING, "Crafting"));
                }
            });

    {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 40, Block.STONE);

            if (unit.absoluteStart().blockY() < 40 && unit.absoluteEnd().blockY() > 40) {
                unit.modifier().setBlock(unit.absoluteStart().blockX(), 40, unit.absoluteStart().blockZ(), Block.TORCH);
            }
        });
        instanceContainer.setChunkSupplier(LightingChunk::new);
        instanceContainer.setTimeRate(0);
        instanceContainer.setTime(12000);

        inventory = new Inventory(InventoryType.CHEST_1_ROW, Component.text("Test inventory"));
        inventory.setItemStack(3, ItemStack.of(Material.DIAMOND, 34));
    }

    private final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    public void init() {
        var eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addChild(DEMO_NODE);

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        MinestomAdventure.COMPONENT_TRANSLATOR = (c, l) -> c;

        eventHandler.addListener(ServerTickMonitorEvent.class, event -> LAST_TICK.set(event.tickMonitor()));

        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (LAST_TICK.get() == null || MinecraftServer.getConnectionManager().getOnlinePlayerCount() == 0)
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = LAST_TICK.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                    .append(Component.newline())
                    .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))
                    .append(Component.newline())
                    .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    public static ItemStack getFoodItem(int consumeTicks) {
        return ItemStack.builder(Material.IRON_NUGGET)
                .amount(64)
                .set(ItemComponent.CONSUMABLE, new Consumable(
                        (float) consumeTicks / 20,
                        ItemAnimation.EAT,
                        SoundEvent.BLOCK_CHAIN_STEP,
                        true,
                        new ArrayList<>()))
                .build();
    }
}
