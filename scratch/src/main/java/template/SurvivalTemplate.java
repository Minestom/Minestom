package template;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.kyori.adventure.text.Component;
import net.minestom.scratch.block.BlockEntityHandler;
import net.minestom.scratch.command.LegacyStringArrayCommands;
import net.minestom.scratch.entity.*;
import net.minestom.scratch.event.PacketWaiter;
import net.minestom.scratch.interest.Broadcast;
import net.minestom.scratch.inventory.InventoryHolder;
import net.minestom.scratch.listener.ScratchFeature;
import net.minestom.scratch.network.NetworkContext;
import net.minestom.scratch.registry.ScratchRegistryTools;
import net.minestom.scratch.velocity.ScratchVelocityTools;
import net.minestom.scratch.world.TrackedWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.coordinate.ChunkRangeUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.entity.pathfinding.PPath;
import net.minestom.server.entity.pathfinding.PathGenerator;
import net.minestom.server.entity.pathfinding.generators.GroundNodeGenerator;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeCategory;
import net.minestom.server.recipe.RecipeCompute;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.SlotUtils;
import net.minestom.server.world.DimensionType;
import org.jctools.queues.SpscUnboundedArrayQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.minestom.scratch.entity.EntityLogic.Goal;

/**
 * Server attempting to replicate a survival experience.
 * <p>
 * Expect features to be missing, and added over time.
 */
public final class SurvivalTemplate {
    private static final SocketAddress ADDRESS = new InetSocketAddress("0.0.0.0", 25565);
    private static final int VIEW_DISTANCE = 8;

    public static void main(String[] args) throws Exception {
        new SurvivalTemplate();
    }

    private final AtomicInteger lastEntityId = new AtomicInteger();
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.INET);
    private final ConcurrentLinkedQueue<PlayerInfo> waitingPlayers = new ConcurrentLinkedQueue<>();

    private final Broadcast serverBroadcast = new Broadcast();
    private final Instance overworld = new Instance(new TrackedWorld(ScratchRegistryTools.DIMENSION_REGISTRY.get(DimensionType.OVERWORLD),
            ScratchRegistryTools.BIOME_REGISTRY, unit -> unit.modifier().fillHeight(0, 48, Block.STONE)));
    private final Instance nether = new Instance(new TrackedWorld(ScratchRegistryTools.DIMENSION_REGISTRY.get(DimensionType.THE_NETHER),
            ScratchRegistryTools.BIOME_REGISTRY, unit -> unit.modifier().fillHeight(0, 48, Block.STONE)));
    private final Instance end = new Instance(new TrackedWorld(ScratchRegistryTools.DIMENSION_REGISTRY.get(DimensionType.THE_END),
            ScratchRegistryTools.BIOME_REGISTRY, unit -> unit.modifier().fillHeight(0, 48, Block.STONE)));
    private final Set<Instance> worlds = Set.of(overworld, nether, end);

    private final Map<Integer, Player> players = new HashMap<>();
    private final Map<Integer, Entity> entities = new HashMap<>();
    private final Map<Integer, ItemEntity> items = new HashMap<>();

    private final List<Recipe> recipes = List.of(
            new Recipe("minecraft:stick",
                    new Recipe.Shaped("minecraft:stick", RecipeCategory.Crafting.MISC,
                            1, 2,
                            List.of(
                                    new Recipe.Ingredient(ItemStack.of(Material.OAK_PLANKS)),
                                    new Recipe.Ingredient(ItemStack.of(Material.OAK_PLANKS))
                            ),
                            ItemStack.of(Material.STICK, 4), false)),
            new Recipe("minecraft:crafting_table",
                    new Recipe.Shaped("group",
                            RecipeCategory.Crafting.MISC,
                            2, 2,
                            List.of(
                                    new Recipe.Ingredient(ItemStack.of(Material.OAK_PLANKS)),
                                    new Recipe.Ingredient(ItemStack.of(Material.OAK_PLANKS)),
                                    new Recipe.Ingredient(ItemStack.of(Material.OAK_PLANKS)),
                                    new Recipe.Ingredient(ItemStack.of(Material.OAK_PLANKS))
                            ),
                            ItemStack.of(Material.CRAFTING_TABLE), false)),
            new Recipe("minecraft:furnace",
                    new Recipe.Shaped("minecraft:furnace", RecipeCategory.Crafting.MISC,
                            3, 3,
                            List.of(
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                                    new Recipe.Ingredient(),
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                                    new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE))
                            ),
                            ItemStack.of(Material.FURNACE), false)),
            new Recipe(
                    "minestom:test2",
                    new Recipe.Shapeless("minestom:test2", RecipeCategory.Crafting.MISC,
                            List.of(
                                    new Recipe.Ingredient(ItemStack.of(Material.DIRT))
                            ),
                            ItemStack.builder(Material.GOLD_BLOCK)
                                    .set(ItemComponent.CUSTOM_NAME, Component.text("abc"))
                                    .build())
            ),
            new Recipe(
                    "minestom:stone",
                    new Recipe.Smelting("abc", RecipeCategory.Cooking.BLOCKS,
                            new Recipe.Ingredient(ItemStack.of(Material.COBBLESTONE)),
                            ItemStack.of(Material.STONE),
                            0, 0)
            )
    );

    SurvivalTemplate() throws Exception {
        server.bind(ADDRESS);
        System.out.println("Server started on: " + ADDRESS);
        Thread.startVirtualThread(this::listenCommands);
        Thread.startVirtualThread(this::listenConnections);
        ticks();
        server.close();
        System.out.println("Server stopped");
    }

    void listenCommands() {
        Scanner scanner = new Scanner(System.in);
        while (!stop.get()) {
            final String line = scanner.nextLine();
            switch (line) {
                case "stop" -> {
                    stop.set(true);
                    System.out.println("Stopping server...");
                }
                case "gc" -> System.gc();
            }
        }
    }

    void listenConnections() {
        while (!stop.get()) {
            try {
                final SocketChannel client = server.accept();
                System.out.println("Accepted connection from " + client.getRemoteAddress());
                Connection connection = new Connection(client);
                Thread.startVirtualThread(connection::networkLoopRead);
                Thread.startVirtualThread(connection::networkLoopWrite);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void ticks() {
        int keepAliveId = 0;
        while (!stop.get()) {
            final long time = System.nanoTime();
            // Connect waiting players
            PlayerInfo playerInfo;
            while ((playerInfo = waitingPlayers.poll()) != null) {
                final Player player = new Player(playerInfo, overworld, new Pos(0, 55, 0));
                this.players.put(player.id, player);
            }
            // Tick playing players
            List<Runnable> toRemove = new ArrayList<>();
            final boolean sendKeepAlive = keepAliveId++ % (20 * 20) == 0;
            for (Player player : players.values()) {
                if (sendKeepAlive) player.sendPacket(new KeepAlivePacket(keepAliveId));
                if (!player.tick()) toRemove.add(player::unregister);
            }
            // Tick entities
            for (Entity entity : entities.values()) {
                if (!entity.tick()) toRemove.add(entity::unregister);
            }
            for (ItemEntity item : items.values()) {
                if (!item.tick()) toRemove.add(item::unregister);
            }
            // Remove disconnected entities
            for (Runnable runnable : toRemove) runnable.run();
            // Forward broadcast/view packets
            this.serverBroadcast.process();
            {
                final long heapUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                final double elapsed = (System.nanoTime() - time) / 1_000_000.0;
                final PlayerListHeaderAndFooterPacket packet = new PlayerListHeaderAndFooterPacket(
                        Component.text("Welcome to Minestom Survival!"),
                        Component.text("Tick: " + String.format("%.2f", elapsed) + "ms")
                                .append(Component.newline())
                                .append(Component.text("Heap: " + heapUsage / 1024 / 1024 + "MB"))
                );
                players.values().forEach(player -> player.sendPacket(packet));
            }
            try {
                //noinspection BusyWait
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    final class Connection {
        final SocketChannel client;
        final NetworkContext.Async networkContext = new NetworkContext.Async();
        final SpscUnboundedArrayQueue<ClientPacket> packetQueue = new SpscUnboundedArrayQueue<>(2500);
        volatile boolean online = true;

        PlayerInfo playerInfo;

        Connection(SocketChannel client) {
            this.client = client;
        }

        void networkLoopRead() {
            while (online) {
                this.online = this.networkContext.read(buffer -> {
                    try {
                        buffer.readChannel(client);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, this::handleAsyncPacket);
            }
        }

        void networkLoopWrite() {
            while (online) {
                this.online = this.networkContext.write(buffer -> {
                    try {
                        buffer.writeChannel(client);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        void handleAsyncPacket(ClientPacket packet) {
            if (packet instanceof ClientFinishConfigurationPacket) {
                waitingPlayers.offer(playerInfo);
                return;
            }
            if (networkContext.state() == ConnectionState.PLAY) {
                packetQueue.add(packet);
                return;
            }
            switch (packet) {
                case StatusRequestPacket ignored -> {
                    this.networkContext.write(new ResponsePacket("""
                            {
                                "version": {
                                    "name": "%s",
                                    "protocol": %s
                                },
                                "players": {
                                    "max": 100,
                                    "online": 0
                                },
                                "description": {
                                    "text": "Awesome Minestom"
                                },
                                "enforcesSecureChat": false,
                                "previewsChat": false
                            }
                            """.formatted(MinecraftServer.VERSION_NAME, MinecraftServer.PROTOCOL_VERSION)));
                }
                case ClientPingRequestPacket pingRequestPacket -> {
                    this.networkContext.write(new PingResponsePacket(pingRequestPacket.number()));
                }
                case ClientLoginStartPacket startPacket -> {
                    // TODO: remove random UUID, currently necessary to test with multiple players
                    this.playerInfo = new PlayerInfo(this, startPacket.username(), UUID.randomUUID());
                    this.networkContext.write(new LoginSuccessPacket(startPacket.profileId(), startPacket.username(), 0, false));
                }
                case ClientLoginAcknowledgedPacket ignored -> {
                    this.networkContext.write(ScratchRegistryTools.REGISTRY_PACKETS);
                    this.networkContext.write(new FinishConfigurationPacket());
                }
                default -> {
                }
            }
        }
    }

    record PlayerInfo(Connection connection, String username, UUID uuid) {
    }

    final class Instance {
        final TrackedWorld blockHolder;
        final BlockEntityHandler<Instance, Player> blockEntityHandler = new BlockEntityHandler<>(Map.of(
                Block.CHEST.id(), new BlockEntityHandler.Entry<>(
                        (w, point) -> new ChestHandler(w, point, new InventoryHolder.Container(Component.text("Chest"), InventoryType.CHEST_3_ROW))
                ),
                Block.CRAFTING_TABLE.id(), new BlockEntityHandler.Entry<>(CraftingTableHandler::new),
                Block.FURNACE.id(), new BlockEntityHandler.Entry<>(FurnaceHandler::new)
        ));
        final Broadcast.World synchronizer = serverBroadcast.makeWorld(VIEW_DISTANCE);
        final WorldBorder worldBorder = new WorldBorder(29999984, 0, 0, 1, 300);

        public Instance(TrackedWorld blockHolder) {
            this.blockHolder = blockHolder;
        }
    }

    final class ChestHandler implements BlockEntityHandler.Action<Player> {
        private final Instance instance;
        private final Point point;
        private final InventoryHolder.Container container;

        public ChestHandler(Instance instance, Point point, InventoryHolder.Container container) {
            this.instance = instance;
            this.point = point;
            this.container = container;
        }

        @Override
        public void onBreak(Player player) {
            for (ItemStack item : container.inventory()) {
                if (item.isAir()) continue;
                // Drop item with random velocity
                ItemEntity itemEntity = new ItemEntity(player.instance, Pos.fromPoint(point).add(0.5, 0.5, 0.5), item);
                itemEntity.velocity = new Vec(Math.random() * 0.1 - 0.05, Math.random() * 0.1 - 0.05, Math.random() * 0.1 - 0.05);
                SurvivalTemplate.this.items.put(itemEntity.id, itemEntity);
            }
        }

        @Override
        public void onInteract(Player player) {
            player.inventoryHolder.openContainer(container);
            var openAction = new BlockActionPacket(point, (byte) 1, (byte) container.viewers().size(), Block.CHEST);
            instance.synchronizer.signalAt(point.chunkX(), point.chunkZ(), openAction);

            player.packetWaiter.onReceived(ClientCloseWindowPacket.class, packet -> {
                if (packet.windowId() != container.id()) return;
                player.inventoryHolder.closeContainer();
                var closeAction = new BlockActionPacket(point, (byte) 1, (byte) container.viewers().size(), Block.CHEST);
                instance.synchronizer.signalAt(point.chunkX(), point.chunkZ(), closeAction);
            });
        }
    }

    final class CraftingTableHandler implements BlockEntityHandler.Action<Player> {
        private final Instance instance;
        private final Point point;
        private final WeakHashMap<Player, InventoryHolder.Container> craftingPlayers = new WeakHashMap<>();

        public CraftingTableHandler(Instance instance, Point point) {
            this.instance = instance;
            this.point = point;
        }

        @Override
        public void onBreak(Player player) {
            for (Player p : craftingPlayers.keySet()) {
                dropContent(p, point);
            }
        }

        @Override
        public void onInteract(Player player) {
            InventoryHolder.Container container = new InventoryHolder.Container(Component.text("Crafting Table"), InventoryType.CRAFTING);
            craftingPlayers.put(player, container);
            player.inventoryHolder.openContainer(container);
            player.packetWaiter.onReceived(ClientCloseWindowPacket.class, packet -> {
                if (packet.windowId() != container.id()) return;
                dropContent(player, point);
            });
        }

        private void dropContent(Player player, Point point) {
            InventoryHolder.Container container = craftingPlayers.get(player);
            if (container == null) return;
            for (int i = 1; i < container.inventory().length; i++) {
                ItemStack item = container.inventory()[i];
                if (item.isAir()) continue;
                // Drop item with random velocity
                ItemEntity itemEntity = new ItemEntity(instance, Pos.fromPoint(point).add(0.5, 1, 0.5), item);
                itemEntity.velocity = new Vec(Math.random() * 0.1 - 0.05, 0, Math.random() * 0.1 - 0.05);
                SurvivalTemplate.this.items.put(itemEntity.id, itemEntity);
            }
        }
    }

    final class FurnaceHandler implements BlockEntityHandler.Action<Player> {
        private final Instance instance;
        private final Point point;
        private final InventoryHolder.Container container = new InventoryHolder.Container(Component.text("Furnace"), InventoryType.FURNACE);

        public FurnaceHandler(Instance instance, Point point) {
            this.instance = instance;
            this.point = point;
        }

        @Override
        public void onBreak(Player player) {
            for (int i = 0; i < 2; i++) {
                ItemStack item = container.inventory()[i];
                if (item.isAir()) continue;
                // Drop item with random velocity
                ItemEntity itemEntity = new ItemEntity(instance, Pos.fromPoint(point).add(0.5, 1, 0.5), item);
                itemEntity.velocity = new Vec(Math.random() * 0.1 - 0.05, 0, Math.random() * 0.1 - 0.05);
                SurvivalTemplate.this.items.put(itemEntity.id, itemEntity);
            }
        }

        @Override
        public void onInteract(Player player) {
            player.inventoryHolder.openContainer(container);
        }
    }

    sealed interface Actor {
        int id();

        Pos position();
    }

    final class ItemEntity implements Actor {
        private static final int PICKUP_DELAY = 10;
        private final int id = lastEntityId.incrementAndGet();
        private final UUID uuid = UUID.randomUUID();
        final EntityType type = EntityType.ITEM;
        final Instance instance;
        final Broadcast.World.Entry synchronizerEntry;

        final Aerodynamics aerodynamics;

        final MetaHolder metaHolder = new MetaHolder(id);

        Pos position;
        Vec velocity = Vec.ZERO;
        boolean onGround;
        ItemStack itemStack;
        int tickAlive = 0;

        final Supplier<List<ServerPacket.Play>> initPackets = () -> EntityInitPackets.entityInit(id, ItemEntity.this.uuid, type, position,
                Map.of(), metaHolder.metaDataPacket());
        final Supplier<List<ServerPacket.Play>> destroyPackets = () -> EntityInitPackets.entityDestroy(id);

        ItemEntity(Instance instance, Pos position, ItemStack itemStack) {
            this.instance = instance;
            this.position = position;
            this.itemStack = itemStack;

            this.synchronizerEntry = instance.synchronizer.makeEntry(id, position, initPackets, destroyPackets);
            this.metaHolder.set(MetadataDef.ItemEntity.ITEM, itemStack);
            this.aerodynamics = new Aerodynamics(type.registry().acceleration(), 0.98, 1 - type.registry().drag());
        }

        boolean tick() {
            PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity, type.registry().boundingBox(),
                    instance.worldBorder, instance.blockHolder, aerodynamics,
                    false, true, onGround, false, null);

            this.position = physicsResult.newPosition();
            this.velocity = physicsResult.newVelocity();
            this.onGround = physicsResult.isOnGround();

            synchronizerEntry.move(physicsResult.newPosition());
            if (!velocity.isZero()) synchronizerEntry.signalLocal(new EntityVelocityPacket(id, velocity.mul(8000f)));

            this.tickAlive++;
            if (tickAlive >= PICKUP_DELAY) {
                for (Player player : players.values()) {
                    if (player.position.distance(position) < type.registry().boundingBox().width() + 1) {
                        if (!player.inventoryHolder.canAddItem(itemStack)) continue;
                        player.inventoryHolder.addItem(itemStack);
                        player.sendPacket(player.inventoryHolder.itemsPacket());
                        player.sendPacket(new CollectItemPacket(id, player.id, itemStack.amount()));
                        this.synchronizerEntry.signalLocal(new CollectItemPacket(id, player.id, itemStack.amount()));
                        return false;
                    }
                }
            }
            return true;
        }

        private void unregister() {
            SurvivalTemplate.this.items.remove(id);
            synchronizerEntry.unmake();
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public Pos position() {
            return position;
        }
    }

    final class Entity implements Actor {
        private final int id = lastEntityId.incrementAndGet();
        private final UUID uuid = UUID.randomUUID();
        final EntityType type;
        final Instance instance;
        final Broadcast.World.Entry synchronizerEntry;

        final Aerodynamics aerodynamics;

        Pos position;
        Vec velocity = Vec.ZERO;
        boolean onGround;

        PPath path;
        NodeGenerator generator = new GroundNodeGenerator();

        final Supplier<List<ServerPacket.Play>> initPackets = () -> EntityInitPackets.entityInit(id, Entity.this.uuid, Entity.this.type, position,
                Map.of(), null);
        final Supplier<List<ServerPacket.Play>> destroyPackets = () -> EntityInitPackets.entityDestroy(id);

        Actor target;
        final EntityLogic logic = new EntityLogic(
                //new Goal.Revenge(),
                //new Goal.ActiveTarget(EntityType.PLAYER),
                //new Goal.LookAtTarget(),
                new Goal.LookAround(50),
                new Goal.Wander(5, 50)
        );

        Entity(EntityType type, Instance instance, Pos position) {
            this.type = type;
            this.instance = instance;
            this.position = position;
            this.synchronizerEntry = instance.synchronizer.makeEntry(id, position, initPackets, destroyPackets);
            this.aerodynamics = new Aerodynamics(type.registry().acceleration(), 0.91, 1 - type.registry().drag());
        }

        boolean tick() {
            processAI();
            final Vec pathVelocity = pathVelocity();
            if (!pathVelocity.isZero()) {
                this.velocity = pathVelocity;
                this.position = position.withDirection(pathVelocity);
            }
            PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity, type.registry().boundingBox(),
                    instance.worldBorder, instance.blockHolder, aerodynamics,
                    false, true, onGround, false, null);

            this.position = physicsResult.newPosition();
            this.velocity = physicsResult.newVelocity();
            this.onGround = physicsResult.isOnGround();

            synchronizerEntry.move(physicsResult.newPosition());
            synchronizerEntry.signalLocal(new EntityTeleportPacket(id, physicsResult.newPosition(), onGround));
            synchronizerEntry.signalLocal(new EntityHeadLookPacket(id, position.yaw()));
            if (!velocity.isZero()) synchronizerEntry.signalLocal(new EntityVelocityPacket(id, velocity.mul(8000f)));
            return true;
        }

        private void processAI() {
            List<EntityLogic.Action> actions = logic.process(target != null);
            for (EntityLogic.Action action : actions) {
                switch (action) {
                    case EntityLogic.Action.Attack attack -> {
                    }
                    case EntityLogic.Action.SearchTarget searchTarget -> {
                    }
                    case EntityLogic.Action.SetPath setPath -> {
                        moveTo(position.add(setPath.point()));
                    }
                    case EntityLogic.Action.LookAt lookAt -> {
                        this.position = position.withDirection(lookAt.point());
                    }
                }
            }
        }

        private Vec pathVelocity() {
            var path = this.path;
            if (path == null) return Vec.ZERO;
            if (path.getNodes().isEmpty()) return Vec.ZERO;
            Point currentTarget = path.getCurrent();
            if (currentTarget == null) return Vec.ZERO;
            Point nextTarget = path.getNext();

            if (nextTarget == null) {
                path.setState(PPath.State.INVALID);
                return Vec.ZERO;
            }

            //boolean nextIsRepath = nextTarget.sameBlock(Pos.ZERO);
            final double speed = 0.1;
            final Vec direction = Vec.fromPoint(currentTarget).sub(position.asVec()).normalize();
            Vec result = direction.mul(speed);
            //nodeFollower.moveTowards(currentTarget, nodeFollower.movementSpeed(), nextIsRepath ? currentTarget : nextTarget);

            if (position.sameBlock(currentTarget)) path.next();
            else if (path.getCurrentType() == PNode.Type.JUMP) result = result.add(0, 1.5 / 20, 0);
            return result;
        }

        void moveTo(Point point) {
            this.path = PathGenerator.generate(instance.blockHolder, position, point,
                    0.1, 50, 20,
                    type.registry().boundingBox(), true, generator, () -> System.out.println("finished"));
        }

        private void unregister() {
            SurvivalTemplate.this.entities.remove(id);
            synchronizerEntry.unmake();
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public Pos position() {
            return position;
        }
    }

    final class Player implements Actor {
        private final int id = lastEntityId.incrementAndGet();
        private final Connection connection;
        private final String username;
        private final UUID uuid;

        Instance instance;
        Broadcast.World.Entry synchronizerEntry;
        GameMode gameMode = GameMode.SURVIVAL;
        Pos position;
        Pos oldPosition;

        final BlockInteractionHandler blockInteractionHandler = new BlockInteractionHandler(id, this::sendPacket, play -> synchronizerEntry.signalLocal(play));
        final MetaHolder metaHolder = new MetaHolder(id, play -> {
            sendPacket(play);
            this.synchronizerEntry.signalLocal(play);
        });
        final HealthHandler healthHandler = new HealthHandler(id, this::sendPacket, play -> synchronizerEntry.signalLocal(play));
        final InventoryHolder inventoryHolder = new InventoryHolder(id, this::sendPacket, play -> synchronizerEntry.signalLocal(play));
        final PotionHolder potionHolder = new PotionHolder(id, play -> {
            sendPacket(play);
            this.synchronizerEntry.signalLocal(play);
        });

        final PacketWaiter packetWaiter = new PacketWaiter();

        final Supplier<List<ServerPacket.Play>> initPackets = () -> EntityInitPackets.playerInit(id, Player.this.uuid, position,
                inventoryHolder.equipments(), metaHolder.metaDataPacket());
        final Supplier<List<ServerPacket.Play>> destroyPackets = () -> EntityInitPackets.playerDestroy(id);
        final BiConsumer<List<ServerPacket.Play>, int[]> packetsConsumer = (plays, exceptions) ->
                Player.this.connection.networkContext.write(new NetworkContext.Packet.PlayList(plays, exceptions));

        final ScratchFeature.Messaging messaging;
        final ScratchFeature.Movement movement;
        final ScratchFeature.ChunkLoading chunkLoading;
        final ScratchFeature.EntityInteract entityInteract;

        final LegacyStringArrayCommands commands = new LegacyStringArrayCommands(
                Map.of(
                        "help", args -> sendMessage(Component.text("No help for u.")),
                        "say", args -> serverBroadcast.broadcast(new SystemChatPacket(Component.text("[" + Player.this.username + "] ")
                                .append(Component.text(args)), false)),
                        "dimension", s -> {
                            final Instance newWorld = switch (s) {
                                case "overworld" -> overworld;
                                case "nether" -> nether;
                                case "end" -> end;
                                default -> null;
                            };
                            if (newWorld == null) {
                                sendMessage(Component.text("Invalid dimension"));
                                return;
                            }
                            if (newWorld == instance) {
                                sendMessage(Component.text("Already in that dimension"));
                                return;
                            }
                            switchInstance(newWorld);
                            sendMessage(Component.text("Switched to " + s));
                        },
                        "tp", s -> {
                            final String[] split = s.split(" ");
                            if (split.length != 3) {
                                sendMessage(Component.text("Invalid arguments"));
                                return;
                            }
                            final Pos target = new Pos(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                            teleport(target);
                            sendMessage(Component.text("Teleported to " + target));
                        },
                        "gamemode", s -> {
                            try {
                                final GameMode newGameMode = GameMode.valueOf(s.toUpperCase(Locale.ROOT));
                                this.gameMode = newGameMode;
                                sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, newGameMode.id()));
                                sendMessage(Component.text("GameMode: " + s));
                            } catch (IllegalArgumentException e) {
                                sendMessage(Component.text("Invalid GameMode"));
                                return;
                            }
                        },
                        "summon", s -> {
                            final EntityType entityType = EntityType.fromNamespaceId(s);
                            if (entityType == null) {
                                sendMessage(Component.text("Invalid entity type"));
                                return;
                            }
                            var entity = new Entity(entityType, overworld, position);
                            entities.put(entity.id, entity);
                            sendMessage(Component.text("Spawned entity"));
                        },
                        "datacomponent", s -> {
                            final ItemStack handItem = inventoryHolder.getHandItem(PlayerHand.MAIN);
                            if (handItem.isAir()) {
                                sendMessage(Component.text("No item in hand"));
                                return;
                            }
                            final CompoundBinaryTag data = handItem.toItemNBT();
                            final String snbt = TagStringIOExt.writeTag(data);
                            final DataComponentMap components = handItem.material().prototype();
                            sendMessage(Component.text("SNBT: " + snbt));
                            sendMessage(Component.text("Prototype: " + components));
                        }
                )
        );

        Player(PlayerInfo info, Instance spawnInstance, Pos spawnPosition) {
            this.connection = info.connection;
            this.username = info.username;
            this.uuid = info.uuid;

            this.instance = spawnInstance;
            this.position = spawnPosition;
            this.oldPosition = spawnPosition;

            this.synchronizerEntry = instance.synchronizer.makeReceiver(id, position, initPackets, destroyPackets, packetsConsumer);

            this.messaging = new ScratchFeature.Messaging(new ScratchFeature.Messaging.Mapping() {
                @Override
                public Component formatMessage(String message) {
                    return Component.text("<" + username + "> ")
                            .append(Component.text(message));
                }

                @Override
                public void signal(ServerPacket.Play packet) {
                    serverBroadcast.broadcast(packet);
                }
            });

            this.movement = new ScratchFeature.Movement(new ScratchFeature.Movement.Mapping() {
                @Override
                public int id() {
                    return id;
                }

                @Override
                public Pos position() {
                    return position;
                }

                @Override
                public void updatePosition(Pos position) {
                    Player.this.position = position;
                    synchronizerEntry.move(position);
                }

                @Override
                public void signalMovement(ServerPacket.Play packet) {
                    synchronizerEntry.signalLocal(packet);
                }
            });

            this.chunkLoading = new ScratchFeature.ChunkLoading(new ScratchFeature.ChunkLoading.Mapping() {
                @Override
                public int viewDistance() {
                    return VIEW_DISTANCE;
                }

                @Override
                public Pos oldPosition() {
                    return oldPosition;
                }

                @Override
                public ChunkDataPacket chunkPacket(int chunkX, int chunkZ) {
                    return instance.blockHolder.generatePacket(chunkX, chunkZ);
                }

                @Override
                public void sendPacket(ServerPacket.Play packet) {
                    Player.this.sendPacket(packet);
                }
            });

            this.entityInteract = new ScratchFeature.EntityInteract(new ScratchFeature.EntityInteract.Mapping() {
                @Override
                public void left(int id) {
                    Entity entity = entities.get(id);
                    if (entity == null) return;
                    entity.velocity = ScratchVelocityTools.knockback(position, 0.4f, entity.velocity, entity.onGround);
                }

                @Override
                public void right(int id) {
                }
            });

            this.inventoryHolder.addItem(ItemStack.of(Material.STONE, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.CHEST, 64));
            //this.inventoryHolder.addItem( ItemStack.of(Material.DIAMOND_CHESTPLATE));
            //this.inventoryHolder.addItem( ItemStack.of(Material.GOLDEN_CHESTPLATE));
            //this.inventoryHolder.addItem( ItemStack.of(Material.DIAMOND_LEGGINGS));
            this.inventoryHolder.addItem(ItemStack.of(Material.APPLE, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.OAK_PLANKS, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.DIRT, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.OAK_SAPLING, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.BONE_MEAL, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.COBBLESTONE, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.FURNACE, 64));
            this.inventoryHolder.addItem(ItemStack.of(Material.COAL, 64));

            this.connection.networkContext.writePlays(initPackets());

            serverBroadcast.broadcast(getAddPlayerToList());
            for (Player player : players.values()) {
                if (player == this) continue;
                sendPacket(player.getAddPlayerToList());
            }
        }

        void sendMessage(Component message) {
            sendPacket(new SystemChatPacket(message, false));
        }

        private void teleport(Pos target) {
            this.position = target;
            this.synchronizerEntry.move(target);
            this.synchronizerEntry.signalLocal(new EntityTeleportPacket(id, target, false));
            sendPacket(new PlayerPositionAndLookPacket(target, (byte) 0, 0));
        }

        private void switchInstance(Instance newWorld) {
            if (newWorld == instance) return;
            final var oldWorld = instance;
            final var oldSynchronizerEntry = synchronizerEntry;
            oldSynchronizerEntry.unmake();
            this.instance = newWorld;
            this.synchronizerEntry = instance.synchronizer.makeReceiver(id, position, initPackets, destroyPackets, packetsConsumer);

            final DimensionType dimension = instance.blockHolder.dimensionType();
            final DynamicRegistry.Key<DimensionType> dimensionKey = ScratchRegistryTools.DIMENSION_REGISTRY.getKey(dimension);
            final int dimensionId = ScratchRegistryTools.DIMENSION_REGISTRY.getId(dimensionKey);

            RespawnPacket respawnPacket = new RespawnPacket(dimensionId, dimensionKey.name(), 0, gameMode, gameMode,
                    false, false, null, 0, (byte) RespawnPacket.COPY_METADATA);
            sendPacket(respawnPacket);
            sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0));
            ChunkRangeUtils.forChunksInRange(position.chunkX(), position.chunkZ(), VIEW_DISTANCE,
                    (x, z) -> sendPacket(newWorld.blockHolder.generatePacket(x, z)));
            sendPacket(new PlayerPositionAndLookPacket(position, (byte) 0, 0));
        }

        private void sendPacket(ServerPacket.Play packet) {
            this.connection.networkContext.write(packet);
        }

        private List<ServerPacket.Play> initPackets() {
            TrackedWorld blockHolder = instance.blockHolder;
            List<ServerPacket.Play> packets = new ArrayList<>();

            final DimensionType dimension = instance.blockHolder.dimensionType();
            final DynamicRegistry.Key<DimensionType> dimensionKey = ScratchRegistryTools.DIMENSION_REGISTRY.getKey(dimension);
            final int dimensionId = ScratchRegistryTools.DIMENSION_REGISTRY.getId(dimensionKey);
            final JoinGamePacket joinGamePacket = new JoinGamePacket(
                    id, false, List.of(), 0,
                    VIEW_DISTANCE, VIEW_DISTANCE,
                    false, true, false,
                    dimensionId, dimensionKey.name(),
                    0, gameMode, null, false, true,
                    new WorldPos(dimensionKey.name(), Vec.ZERO), 0, false);
            packets.add(joinGamePacket);
            packets.add(commands.generatePacket());
            packets.add(new DeclareRecipesPacket(recipes));
            List<String> recipeIds = recipes.stream().map(Recipe::id).toList();
            packets.add(new UnlockRecipesPacket(0,
                    false, false,
                    false, false,
                    false, false,
                    false, false,
                    recipeIds, recipeIds));
            packets.add(new SpawnPositionPacket(position, 0));
            packets.add(new PlayerPositionAndLookPacket(position, (byte) 0, 0));
            packets.add(getAddPlayerToList());

            packets.add(new UpdateViewDistancePacket(VIEW_DISTANCE));
            packets.add(new UpdateViewPositionPacket(position.chunkX(), position.chunkZ()));
            ChunkRangeUtils.forChunksInRange(position.chunkX(), position.chunkZ(), VIEW_DISTANCE,
                    (x, z) -> packets.add(blockHolder.generatePacket(x, z)));

            packets.add(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f));

            packets.add(inventoryHolder.itemsPacket());
            packets.add(inventoryHolder.equipmentPacket());
            packets.add(healthHandler.healthPacket());

            return packets;
        }

        boolean tick() {
            this.connection.packetQueue.drain(packet -> {
                this.messaging.accept(packet);
                this.movement.accept(packet);
                this.chunkLoading.accept(packet);
                this.entityInteract.accept(packet);
                switch (packet) {
                    case ClientCommandChatPacket commandChatPacket -> commands.consume(commandChatPacket);
                    case ClientHeldItemChangePacket heldItemChangePacket -> {
                        this.inventoryHolder.consume(heldItemChangePacket);
                        this.healthHandler.cancelEating();
                    }
                    case ClientClickWindowPacket clickWindowPacket -> {
                        this.inventoryHolder.consume(clickWindowPacket);
                        var openContainer = inventoryHolder.openContainer();
                        if (openContainer == null) {
                            ItemStack[] craftItems = new ItemStack[]{
                                    inventoryHolder.getItem(SlotUtils.CRAFT_SLOT_1),
                                    inventoryHolder.getItem(SlotUtils.CRAFT_SLOT_2),
                                    inventoryHolder.getItem(SlotUtils.CRAFT_SLOT_3),
                                    inventoryHolder.getItem(SlotUtils.CRAFT_SLOT_4
                                    )};
                            final RecipeCompute.CraftResult result = RecipeCompute.searchCraft(recipes, 2, 2, craftItems);
                            final ItemStack craftItem = result != null ? result.item() : ItemStack.AIR;
                            inventoryHolder.setItem(SlotUtils.CRAFT_RESULT, craftItem);
                            sendPacket(new SetSlotPacket((byte) 0, 0, (short) SlotUtils.convertToPacketSlot(SlotUtils.CRAFT_RESULT), craftItem));
                        } else if (openContainer.type() == InventoryType.CRAFTING) {
                            ItemStack[] craftItems = new ItemStack[3 * 3];
                            for (int i = 0; i < openContainer.inventory().length - 1; i++) {
                                craftItems[i] = openContainer.inventory()[i + 1];
                            }
                            final RecipeCompute.CraftResult result = RecipeCompute.searchCraft(recipes, 3, 3, craftItems);
                            final ItemStack craftItem = result != null ? result.item() : ItemStack.AIR;
                            openContainer.inventory()[0] = craftItem;
                            sendPacket(new SetSlotPacket(openContainer.id(), 0, (short) 0, craftItem));
                        }
                    }
                    case ClientCreativeInventoryActionPacket creativeInventoryActionPacket -> {
                        this.inventoryHolder.consume(creativeInventoryActionPacket);
                    }
                    case ClientCloseWindowPacket closeWindowPacket -> this.inventoryHolder.consume(closeWindowPacket);
                    case ClientUseItemPacket useItemPacket -> {
                        this.inventoryHolder.consume(useItemPacket);
                        final int slot = switch (useItemPacket.hand()) {
                            case MAIN -> inventoryHolder.heldSlot();
                            case OFF -> SlotUtils.OFFHAND_SLOT;
                        };
                        this.healthHandler.startEating(slot, inventoryHolder.getHandItem(useItemPacket.hand()));
                    }
                    case ClientAnimationPacket animationPacket -> {
                        final EntityAnimationPacket.Animation animation = switch (animationPacket.hand()) {
                            case MAIN -> EntityAnimationPacket.Animation.SWING_MAIN_ARM;
                            case OFF -> EntityAnimationPacket.Animation.SWING_OFF_HAND;
                        };
                        this.synchronizerEntry.signalLocal(new EntityAnimationPacket(id, animation));
                    }
                    case ClientPlayerDiggingPacket diggingPacket -> {
                        handle(blockInteractionHandler.consume(diggingPacket, gameMode == GameMode.CREATIVE));
                        switch (diggingPacket.status()) {
                            case UPDATE_ITEM_STATE -> this.healthHandler.cancelEating();
                            case DROP_ITEM -> {
                                final ItemStack currentHand = inventoryHolder.getHandItem(PlayerHand.MAIN);
                                ItemEntity itemEntity = new ItemEntity(instance, position.withY(y -> y + 1.5), currentHand.withAmount(1));
                                itemEntity.velocity = position.direction().mul(0.3);
                                items.put(itemEntity.id, itemEntity);
                                final ItemStack updated = currentHand.withAmount(amount -> amount - 1);
                                inventoryHolder.setItem(inventoryHolder.heldSlot(), updated);
                                if (updated.isAir()) synchronizerEntry.signalLocal(inventoryHolder.equipmentPacket());
                            }
                            case DROP_ITEM_STACK -> {
                                final ItemStack item = inventoryHolder.getHandItem(PlayerHand.MAIN);
                                ItemEntity itemEntity = new ItemEntity(instance, position.withY(y -> y + 1.5), item);
                                itemEntity.velocity = position.direction().mul(0.3);
                                items.put(itemEntity.id, itemEntity);
                                inventoryHolder.setItem(inventoryHolder.heldSlot(), ItemStack.AIR);
                                synchronizerEntry.signalLocal(inventoryHolder.equipmentPacket());
                            }
                            case SWAP_ITEM_HAND -> {
                                final ItemStack mainHand = inventoryHolder.getHandItem(PlayerHand.MAIN);
                                final ItemStack offHand = inventoryHolder.getHandItem(PlayerHand.OFF);
                                inventoryHolder.setHandItem(PlayerHand.MAIN, offHand);
                                inventoryHolder.setHandItem(PlayerHand.OFF, mainHand);
                                synchronizerEntry.signalLocalSelf(inventoryHolder.equipmentPacket());
                            }
                        }
                    }
                    case ClientPlayerBlockPlacementPacket blockPlacementPacket ->
                            handle(blockInteractionHandler.consume(blockPlacementPacket, inventoryHolder.getHandItem(blockPlacementPacket.hand())));
                    case ClientCraftRecipeRequest craftRecipeRequest ->
                            sendPacket(new CraftRecipeResponse(craftRecipeRequest.windowId(), craftRecipeRequest.recipe()));
                    default -> {
                        // Empty
                    }
                }
                this.packetWaiter.consume(packet);
                //System.out.println("packet: " + packet);
            });
            for (HealthHandler.Action action : this.healthHandler.updateEating()) {
                switch (action) {
                    case HealthHandler.Action.ApplyEffect applyEffect ->
                            this.potionHolder.apply(applyEffect.potionEffect());
                    case HealthHandler.Action.ConsumeItem consumeItem -> {
                        this.inventoryHolder.consumeItem(consumeItem.slot());
                        this.synchronizerEntry.signalLocal(inventoryHolder.equipmentPacket());
                    }
                }
            }
            this.potionHolder.updateEffects();
            this.oldPosition = this.position;
            return connection.online;
        }

        private void unregister() {
            serverBroadcast.broadcast(getRemovePlayerToList());
            players.remove(id);
            var synchronizerEntry = this.synchronizerEntry;
            if (synchronizerEntry != null) synchronizerEntry.unmake();
        }

        private void handle(List<BlockInteractionHandler.Action> actions) {
            for (BlockInteractionHandler.Action action : actions) {
                switch (action) {
                    case BlockInteractionHandler.Action.BreakBlock breakBlock -> {
                        final Point point = breakBlock.blockPosition();
                        final Block block = instance.blockHolder.getBlock(point);
                        if (block.compare(Block.AIR)) return;
                        instance.blockEntityHandler.brk(this, point);
                        instance.blockHolder.setBlock(point, Block.AIR);
                        instance.synchronizer.signalAt(point, new BlockChangePacket(point, Block.AIR));
                    }
                    case BlockInteractionHandler.Action.PlaceBlock placeBlock -> {
                        final Point point = placeBlock.blockPosition();
                        final ItemStack item = inventoryHolder.getHandItem(placeBlock.hand());
                        final Block block = item.material().registry().block();
                        if (block == null) return;
                        instance.blockEntityHandler.place(this, instance, point, block);
                        instance.blockHolder.setBlock(point, block);
                        instance.synchronizer.signalAt(point, new BlockChangePacket(point, block));
                        inventoryHolder.consumeItem(placeBlock.hand());
                        synchronizerEntry.signalLocal(inventoryHolder.equipmentPacket());
                    }
                    case BlockInteractionHandler.Action.InteractBlock interactBlock -> {
                        final Point point = interactBlock.blockPosition();
                        instance.blockEntityHandler.interact(this, point);
                    }
                }
            }
        }

        private PlayerInfoUpdatePacket getAddPlayerToList() {
            final var infoEntry = new PlayerInfoUpdatePacket.Entry(uuid, username, List.of(),
                    true, 1, gameMode, null, null);
            return new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                    List.of(infoEntry));
        }

        private PlayerInfoRemovePacket getRemovePlayerToList() {
            return new PlayerInfoRemovePacket(uuid);
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public Pos position() {
            return position;
        }
    }
}
