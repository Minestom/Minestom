package net.minestom.demo;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.FeatureFlag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.Notification;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.dialog.*;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.inventory.CreativeInventoryActionEvent;
import net.minestom.server.event.item.*;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.packet.server.common.CustomReportDetailsPacket;
import net.minestom.server.network.packet.server.common.ServerLinksPacket;
import net.minestom.server.network.packet.server.play.TrackedWaypointPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.io.IOException;
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
                final Entity source = event.getEntity();
                final Entity entity = event.getTarget();

                entity.takeKnockback(0.4f, Math.sin(source.getPosition().yaw() * 0.017453292), -Math.cos(source.getPosition().yaw() * 0.017453292));

                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    target.damage(Damage.fromEntity(source, 5));
                }

                if (source instanceof Player) {
                    ((Player) source).sendMessage("You attacked something!");
                }
            })
            .addListener(PlayerDeathEvent.class, event -> event.setChatMessage(Component.text("custom death message")))
            .addListener(PickupItemEvent.class, event -> {
                final Entity entity = event.getLivingEntity();
                if (entity instanceof Player) {
                    // Cancel event if player does not have enough inventory space
                    final ItemStack itemStack = event.getItemEntity().getItemStack();
                    event.setCancelled(!((Player) entity).getInventory().addItemStack(itemStack));
                }
            })
            .addListener(ItemDropEvent.class, event -> {
                final Player player = event.getPlayer();
                ItemStack droppedItem = event.getItemStack();

                Pos playerPos = player.getPosition();
                ItemEntity itemEntity = new ItemEntity(droppedItem);
                itemEntity.setPickupDelay(Duration.of(500, TimeUnit.MILLISECOND));
                itemEntity.setInstance(player.getInstance(), playerPos.withY(y -> y + 1.5));
                Vec velocity = playerPos.direction().mul(6);
                itemEntity.setVelocity(velocity);
            })
            .addListener(PlayerDisconnectEvent.class, event -> System.out.println("DISCONNECTION " + event.getPlayer().getUsername()))
            .addListener(AsyncPlayerConfigurationEvent.class, event -> {
                final Player player = event.getPlayer();

                // Show off adding and removing feature flags
                event.removeFeatureFlag(FeatureFlag.TRADE_REBALANCE); // not enabled by default, just removed for demonstration

                var instances = MinecraftServer.getInstanceManager().getInstances();
                Instance instance = instances.stream().skip(new Random().nextInt(instances.size())).findFirst().orElse(null);
                event.setSpawningInstance(instance);
                int x = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
                int z = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
                player.setRespawnPoint(new Pos(0, 40f, 0));
            })
            .addListener(PlayerSpawnEvent.class, event -> {
                final Player player = event.getPlayer();
                player.setGameMode(GameMode.CREATIVE);
                player.setPermissionLevel(4);
//                ItemStack itemStack = ItemStack.builder(Material.STONE)
//                        .amount(64)
//                        .set(DataComponents.CAN_PLACE_ON, new BlockPredicates(new BlockPredicate(new BlockTypeFilter.Blocks(Block.STONE), null, null)))
//                        .set(DataComponents.CAN_BREAK, new BlockPredicates(new BlockPredicate(new BlockTypeFilter.Blocks(Block.DIAMOND_ORE), null, null)))
//                        .build();
//                player.getInventory().addItemStack(itemStack);

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
                        .set(DataComponents.BUNDLE_CONTENTS, List.of(
                                ItemStack.of(Material.DIAMOND, 5),
                                ItemStack.of(Material.RABBIT_FOOT, 5)
                        ))
                        .build();
                player.getInventory().addItemStack(bundle);

                PlayerInventory inventory = event.getPlayer().getInventory();
                inventory.addItemStack(getFoodItem(20));
                inventory.addItemStack(getFoodItem(10000));
                inventory.addItemStack(getFoodItem(Integer.MAX_VALUE));
                inventory.addItemStack(ItemStack.of(Material.PURPLE_BED));

                if (event.isFirstSpawn()) {
                    event.getPlayer().sendNotification(new Notification(
                            Component.text("Welcome!"),
                            FrameType.TASK,
                            Material.IRON_SWORD
                    ));

                    player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 0.5f, 1f));

                    var happyGhast = new LivingEntity(EntityType.HAPPY_GHAST);
                    happyGhast.setNoGravity(true);
                    happyGhast.setBodyEquipment(ItemStack.of(Material.GREEN_HARNESS));
                    happyGhast.setInstance(player.getInstance(), new Pos(10, 43, 5, 45, 0));

                    player.sendPacket(new TrackedWaypointPacket(TrackedWaypointPacket.Operation.TRACK, new TrackedWaypointPacket.Waypoint(
                            Either.left(happyGhast.getUuid()),
                            TrackedWaypointPacket.Icon.DEFAULT,
                            new TrackedWaypointPacket.Target.Vec3i(happyGhast.getPosition())
                    )));
                }
            })
            .addListener(PlayerChatEvent.class, event -> {
                var dialog = new Dialog.MultiAction(
                        new DialogMetadata(
                                Component.text("Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?Are you sure you want to confirm?").hoverEvent(HoverEvent.showText(Component.text("Hover text here"))),
                                null, true, false,
                                DialogAfterAction.CLOSE,
                                List.of(
                                        new DialogBody.PlainMessage(Component.text("plain message here").hoverEvent(HoverEvent.showText(Component.text("Hover text here"))), DialogBody.PlainMessage.DEFAULT_WIDTH),
                                        new DialogBody.Item(ItemStack.of(Material.DIAMOND, 5),
                                                new DialogBody.PlainMessage(Component.text("item message"), DialogBody.PlainMessage.DEFAULT_WIDTH),
                                                false, true, 16, 16)
                                ),
                                List.of(
                                        new DialogInput.Text("text", DialogInput.DEFAULT_WIDTH * 2, Component.text("Enter some text")
                                                .hoverEvent(HoverEvent.showText(Component.text("Hover text here"))), true, "", Integer.MAX_VALUE, new DialogInput.Text.Multiline(15, null)),
                                        new DialogInput.Boolean("bool", Component.text("Checkbox"), false, "true", "false"),
                                        new DialogInput.SingleOption("single_option", DialogInput.DEFAULT_WIDTH, List.of(
                                                new DialogInput.SingleOption.Option("option1", Component.text("Option 1"), true),
                                                new DialogInput.SingleOption.Option("option2", Component.text("Option 2"), false),
                                                new DialogInput.SingleOption.Option("option3", Component.text("Option 3"), false)
                                        ), Component.text("Single option"), true),
                                        new DialogInput.NumberRange("number_range", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                                "options.generic_value", 0, 500, 250f, 1f),
                                        new DialogInput.NumberRange("number_r2ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                                "options.generic_value", 0, 500, 250f, 1f),
                                        new DialogInput.NumberRange("number_r3ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                                "options.generic_value", 0, 500, 250f, 1f),
                                        new DialogInput.NumberRange("number_r4ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                                "options.generic_value", 0, 500, 250f, 1f),
                                        new DialogInput.NumberRange("number_r5ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                                "options.generic_value", 0, 500, 250f, 1f),
                                        new DialogInput.NumberRange("number_r6ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                                "options.generic_value", 0, 500, 250f, 1f)
                                )
                        ),
                        List.of(
                                new DialogActionButton(Component.text("Done"), null, DialogActionButton.DEFAULT_WIDTH, new DialogAction.DynamicCustom(Key.key("done_action"), null)),
                                new DialogActionButton(Component.text("Done"), null, DialogActionButton.DEFAULT_WIDTH, null)
                        ),
                        null, 2
                );

                event.getPlayer().sendMessage(Component.text("Click for dialog!").clickEvent(ClickEvent.showDialog(dialog)));
            })
            .addListener(PlayerCustomClickEvent.class, event -> {
                String payload = "null";
                if (event.getPayload() != null) {
                    try {
                        payload = MinestomAdventure.tagStringIO().asString(event.getPayload());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(event.getKey() + " -> " + payload);
            })
            .addListener(PlayerPacketOutEvent.class, event -> {
                //System.out.println("out " + event.getPacket().getClass().getSimpleName());
            })
            .addListener(PlayerPacketEvent.class, event -> {

                //System.out.println("in " + event.getPacket().getClass().getSimpleName());
            })
            .addListener(PlayerBlockBreakEvent.class, event -> {
                var instance = event.getInstance();
                var block = event.getBlock();
                var pos = event.getBlockPosition();
                if (block.getProperty("part") == null || block.getProperty("facing") == null) return;
                var isHead = "head".equals(block.getProperty("part"));
                var facing = BlockFace.valueOf(block.getProperty("facing").toUpperCase());
                var other = (isHead ? pos.add(facing.getOppositeFace().toDirection().vec().asPosition()) : pos.add(facing.toDirection().vec().asPosition()));
                var otherBlock = instance.getBlock(other);
                if (otherBlock.id() == block.id()) {
                    instance.setBlock(other, Block.AIR);
                }
            })
            .addListener(PlayerBlockInteractEvent.class, event -> {
                var player = event.getPlayer();
                var instance = event.getInstance();
                var block = event.getBlock();
                if (event.getBlock().key().asMinimalString().endsWith("_bed")) {
                    var pos = event.getBlockPosition();
                    if (block.getProperty("part") == null || block.getProperty("facing") == null) return;
                    var isHead = "head".equals(block.getProperty("part"));
                    var facing = BlockFace.valueOf(block.getProperty("facing").toUpperCase());
                    var other = (isHead ? pos.add(facing.getOppositeFace().toDirection().vec().asPosition()) : pos.add(facing.toDirection().vec().asPosition()));
                    var otherBlock = instance.getBlock(other);
                    if (otherBlock.id() == block.id()) {
                        player.setVelocity(Vec.ZERO);
                        player.swingMainHand();
                        player.enterBed((isHead ? pos : other));
                    }
                }
            })
            .addListener(PlayerLeaveBedEvent.class, event -> {
                var player = event.getPlayer();
                boolean snooze = ThreadLocalRandom.current().nextFloat() < 0.7f;
                if (snooze) {
                    event.setCancelled(true);
                    player.playSound(Sound.sound(SoundEvent.ENTITY_ALLAY_ITEM_THROWN, Sound.Source.PLAYER, 1f, 0.6f));
                    player.sendActionBar(Component.text("I'm too tired to stand up!"));
                } else {
                    player.sendActionBar(Component.empty());
                }
            })
            .addListener(PlayerUseItemOnBlockEvent.class, event -> {
                if (event.getHand() != PlayerHand.MAIN) return;

                var itemStack = event.getItemStack();
                var block = event.getInstance().getBlock(event.getPosition());

                if ("false".equals(block.getProperty("waterlogged")) && itemStack.material().equals(Material.WATER_BUCKET)) {
                    block = block.withProperty("waterlogged", "true");
                } else if ("true".equals(block.getProperty("waterlogged")) && itemStack.material().equals(Material.BUCKET)) {
                    block = block.withProperty("waterlogged", "false");
                } else return;

                event.getInstance().setBlock(event.getPosition(), block);

            })
            .addListener(PlayerBeginItemUseEvent.class, event -> {
                final Player player = event.getPlayer();
                final ItemStack itemStack = event.getItemStack();
                final boolean hasProjectile = !itemStack.get(DataComponents.CHARGED_PROJECTILES, List.of()).isEmpty();
                if (itemStack.material() == Material.CROSSBOW && hasProjectile) {
                    // "shoot" the arrow
                    player.setItemInHand(event.getHand(), itemStack.without(DataComponents.CHARGED_PROJECTILES));
                    event.getPlayer().sendMessage("pew pew!");
                    event.setItemUseDuration(0); // Do not start using the item
                    return;
                }
            })
            .addListener(PlayerFinishItemUseEvent.class, event -> {
                if (event.getItemStack().material() == Material.APPLE) {
                    event.getPlayer().sendMessage("yummy yummy apple");
                }
            })
            .addListener(PlayerCancelItemUseEvent.class, event -> {
                final Player player = event.getPlayer();
                final ItemStack itemStack = event.getItemStack();
                if (itemStack.material() == Material.CROSSBOW && event.getUseDuration() > 25) {
                    player.setItemInHand(event.getHand(), itemStack.with(DataComponents.CHARGED_PROJECTILES, List.of(ItemStack.of(Material.ARROW))));
                    return;
                }
            })
            .addListener(PlayerBlockInteractEvent.class, event -> {
                var block = event.getBlock();
                var rawOpenProp = block.getProperty("open");
                if (rawOpenProp != null) {
                    block = block.withProperty("open", String.valueOf(!Boolean.parseBoolean(rawOpenProp)));
                    event.getInstance().setBlock(event.getBlockPosition(), block);
                }

                if (block.id() == Block.CRAFTING_TABLE.id()) {
                    event.getPlayer().openInventory(new Inventory(InventoryType.CRAFTING, "Crafting"));
                }
            })
            .addListener(CreativeInventoryActionEvent.class, event -> {
                if (event.getClickedItem().material() == Material.APPLE) {
                    event.setClickedItem(ItemStack.of(Material.GOLDEN_APPLE, event.getClickedItem().amount()));
                } else if (event.getClickedItem().material() == Material.ENCHANTED_GOLDEN_APPLE) {
                    event.setCancelled(true);
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

        eventHandler.addListener(ServerTickMonitorEvent.class, event -> LAST_TICK.set(event.getTickMonitor()));

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
                .set(DataComponents.CONSUMABLE, new Consumable(
                        (float) consumeTicks / 20,
                        ItemAnimation.EAT,
                        SoundEvent.BLOCK_CHAIN_STEP,
                        true,
                        new ArrayList<>()))
                .build();
    }
}
