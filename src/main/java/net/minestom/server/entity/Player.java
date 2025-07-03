package net.minestom.server.entity;

import it.unimi.dsi.fastutil.longs.LongArrayPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.pointer.PointersSupplier;
import net.kyori.adventure.resource.ResourcePackCallback;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.advancements.AdvancementTab;
import net.minestom.server.advancements.Notification;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.command.CommandSender;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.*;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.vehicle.PlayerInputs;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupExperienceEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickPreprocessor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.WrittenBookContent;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.*;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import net.minestom.server.network.player.ClientSettings;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.snapshot.EntitySnapshot;
import net.minestom.server.snapshot.PlayerSnapshot;
import net.minestom.server.snapshot.SnapshotImpl;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.statistic.PlayerStatistic;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.chunk.ChunkUpdateLimitChecker;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.worldevent.WorldEvent;
import org.intellij.lang.annotations.MagicConstant;
import org.jctools.queues.MpscArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Those are the major actors of the server
 * <p>
 * You can easily create your own implementation of this and use it with {@link ConnectionManager#setPlayerProvider(PlayerProvider)}.
 */
public class Player extends LivingEntity implements CommandSender, HoverEventSource<ShowEntity>, NamedAndIdentified {
    private static final DynamicRegistry<DimensionType> DIMENSION_TYPE_REGISTRY = MinecraftServer.getDimensionTypeRegistry();

    private static final Component REMOVE_MESSAGE = Component.text("You have been removed from the server without reason.", NamedTextColor.RED);
    private static final Component MISSING_REQUIRED_RESOURCE_PACK = Component.text("Required resource pack was not loaded.", NamedTextColor.RED);

    // Adventure pointer supplier
    protected static final PointersSupplier<Player> PLAYER_POINTERS_SUPPLIER = PointersSupplier.<Player>builder()
            .parent(ENTITY_POINTERS_SUPPLIER)
            .resolving(Identity.NAME, Player::getUsername)
            .resolving(Identity.DISPLAY_NAME, Player::getDisplayName)
            .resolving(Identity.LOCALE, Player::getLocale)
            .build();

    // This probably should be configurable (eg an instance field). However I(matt) am unclear
    // on what it actually does so am holding off on adding API for this until I understand.
    private static final int DEFAULT_SEA_LEVEL = 63;

    private long lastKeepAlive;
    private boolean answerKeepAlive;

    private final GameProfile gameProfile;
    private String username;
    private Component usernameComponent;
    protected final PlayerConnection playerConnection;

    private volatile int latency;
    private Component displayName;
    private PlayerSkin skin;

    private Instance pendingInstance = null;
    private int dimensionTypeId;
    private GameMode gameMode;
    private WorldPos deathLocation;

    /**
     * Keeps track of what chunks are sent to the client, this defines the center of the loaded area
     * in the range of {@link ServerFlag#CHUNK_VIEW_DISTANCE}
     */
    private Vec chunksLoadedByClient = Vec.ZERO;
    private final ReentrantLock chunkQueueLock = new ReentrantLock();
    private final LongPriorityQueue chunkQueue = new LongArrayPriorityQueue(this::compareChunkDistance);
    private boolean needsChunkPositionSync = true;
    private float targetChunksPerTick = 9f; // Always send 9 chunks immediately
    private float pendingChunkCount = 0f; // Number of chunks to send on the current tick (ie 0.5 means we cannot send a chunk yet, 1.5 would send a single chunk with a 0.5 remainder)
    private int maxChunkBatchLead = 1; // Maximum number of batches to send before waiting for a reply
    private int chunkBatchLead = 0; // Number of batches sent without a reply

    final ChunkRange.ChunkConsumer chunkAdder = (chunkX, chunkZ) -> {
        // Load new chunks
        this.instance.loadOptionalChunk(chunkX, chunkZ).thenAccept(this::sendChunk);
    };
    final ChunkRange.ChunkConsumer chunkRemover = (chunkX, chunkZ) -> {
        // Unload old chunks
        sendPacket(new UnloadChunkPacket(chunkX, chunkZ));
        EventDispatcher.call(new PlayerChunkUnloadEvent(this, chunkX, chunkZ));
    };

    private final AtomicInteger teleportId = new AtomicInteger();
    private int receivedTeleportId;

    private final MpscArrayQueue<ClientPacket> packets = new MpscArrayQueue<>(ServerFlag.PLAYER_PACKET_QUEUE_SIZE);
    private final boolean levelFlat;
    private ClientSettings settings = ClientSettings.DEFAULT;
    private float exp;
    private int level;
    private int portalCooldown = 0;

    protected ClickPreprocessor clickPreprocessor = new ClickPreprocessor();
    protected PlayerInventory inventory;
    private AbstractInventory openInventory;
    // Used internally to allow the closing of inventory within the inventory listener
    private boolean didCloseInventory;

    private byte heldSlot;

    private Pos respawnPoint;

    private int food;
    private float foodSaturation;

    private long startItemUseTime;
    private long itemUseTime;
    private PlayerHand itemUseHand;

    // Game state (https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Game_Event)
    private boolean enableRespawnScreen;
    private final ChunkUpdateLimitChecker chunkUpdateLimitChecker = new ChunkUpdateLimitChecker(ServerFlag.PLAYER_CHUNK_UPDATE_LIMITER_HISTORY_SIZE);

    // Experience orb pickup
    protected Cooldown experiencePickupCooldown = new Cooldown(Duration.of(10, TimeUnit.SERVER_TICK));

    private BelowNameTag belowNameTag;

    private int permissionLevel;

    private boolean reducedDebugScreenInformation;
    private boolean hardcore;

    // Abilities
    private boolean flying;
    private boolean allowFlying;
    private boolean instantBreak;
    private float flyingSpeed = 0.05f;
    private float fieldViewModifier = 0.1f;

    private final Map<PlayerStatistic, Integer> statisticValueMap = new Hashtable<>();

    private final PlayerInputs inputs = new PlayerInputs();

    // Resource packs
    record PendingResourcePack(boolean required, @NotNull ResourcePackCallback callback) {
    }

    private final Map<UUID, PendingResourcePack> pendingResourcePacks = new HashMap<>();
    // The future is non-null when a resource pack is in-flight, and completed when all statuses have been received.
    private CompletableFuture<Void> resourcePackFuture = null;

    public Player(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(EntityType.PLAYER, gameProfile.uuid());
        this.gameProfile = gameProfile;
        this.username = gameProfile.name();
        this.usernameComponent = Component.text(username);
        this.playerConnection = playerConnection;

        setRespawnPoint(Pos.ZERO);

        this.inventory = new PlayerInventory();

        setCanPickupItem(true); // By default

        // Allow the server to send the next keep alive packet
        refreshAnswerKeepAlive(true);

        this.gameMode = GameMode.SURVIVAL;
        this.dimensionTypeId = DIMENSION_TYPE_REGISTRY.getId(DimensionType.OVERWORLD); // Default dimension
        this.levelFlat = true;
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);

        // FakePlayer init its connection there
        playerConnectionInit();

        // When in configuration state no metadata updates can be sent.
        metadata.setNotifyAboutChanges(false);
    }

    @ApiStatus.Internal
    public void setPendingOptions(@NotNull Instance pendingInstance, boolean hardcore) {
        // I(mattw) am not a big fan of this function, but somehow we need to store
        // the instance and i didn't like a record in ConnectionManager either.
        this.pendingInstance = pendingInstance;
        this.hardcore = hardcore;
    }

    /**
     * Used when the player is created.
     * Init the player and spawn him.
     * <p>
     * WARNING: executed in the main update thread
     * UNSAFE: Only meant to be used when a socket player connects through the server.
     */
    @ApiStatus.Internal
    public CompletableFuture<Void> UNSAFE_init() {
        final Instance spawnInstance = this.pendingInstance;
        this.pendingInstance = null;

        this.removed = false;
        this.dimensionTypeId = DIMENSION_TYPE_REGISTRY.getId(spawnInstance.getDimensionType());

        final JoinGamePacket joinGamePacket = new JoinGamePacket(
                getEntityId(), this.hardcore, List.of(), 0,
                ServerFlag.CHUNK_VIEW_DISTANCE, ServerFlag.CHUNK_VIEW_DISTANCE,
                false, true, false,
                dimensionTypeId, spawnInstance.getDimensionName(), 0,
                gameMode, null, false, levelFlat,
                deathLocation, portalCooldown, DEFAULT_SEA_LEVEL,
                true);
        sendPacket(joinGamePacket);

        // Start sending inventory updates
        inventory.addViewer(this);

        // Difficulty
        sendPacket(new ServerDifficultyPacket(MinecraftServer.getDifficulty(), true));

        sendPacket(new SpawnPositionPacket(respawnPoint, 0));

        // Reenable metadata notifications as we leave the configuration state
        metadata.setNotifyAboutChanges(true);
        sendPacket(getMetadataPacket());

        // Add player to list with spawning skin
        PlayerSkin profileSkin = null;
        for (GameProfile.Property property : gameProfile.properties()) {
            if (property.name().equals("textures")) {
                profileSkin = new PlayerSkin(property.value(), property.signature());
                break;
            }
        }
        PlayerSkinInitEvent skinInitEvent = new PlayerSkinInitEvent(this, profileSkin);
        EventDispatcher.call(skinInitEvent);
        this.skin = skinInitEvent.getSkin();
        // FIXME: when using Geyser, this line remove the skin of the client
        PacketSendingUtils.broadcastPlayPacket(getAddPlayerToList());

        var connectionManager = MinecraftServer.getConnectionManager();
        for (var player : connectionManager.getOnlinePlayers()) {
            if (player != this) {
                sendPacket(player.getAddPlayerToList());
                if (player.displayName != null) {
                    sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player.infoEntry()));
                }
            }
        }

        //Teams
        for (Team team : MinecraftServer.getTeamManager().getTeams()) {
            sendPacket(team.createTeamsCreationPacket());
        }

        // Commands
        refreshCommands();

        // Recipes
        refreshRecipes();

        // Some client updates
        sendPacket(getPropertiesPacket()); // Send default properties
        triggerStatus((byte) (EntityStatuses.Player.PERMISSION_LEVEL_0 + permissionLevel)); // Set permission level
        refreshHealth(); // Heal and send health packet
        refreshAbilities(); // Send abilities packet

        return setInstance(spawnInstance);
    }

    /**
     * Moves the player immediately to the configuration state. The player is automatically moved
     * to configuration upon finishing login, this method can be used to move them back to configuration
     * after entering the play state.
     *
     * <p>This will result in them being removed from the current instance, player list, etc.</p>
     */
    public void startConfigurationPhase() {
        Check.stateCondition(playerConnection.getConnectionState() != ConnectionState.PLAY,
                "Player must be in the play state for reconfiguration.");
        // Remove the player, then send them back to configuration
        remove(false);
        MinecraftServer.getConnectionManager().transitionPlayToConfig(this);
    }

    /**
     * Used to initialize the player connection
     */
    protected void playerConnectionInit() {
        PlayerConnection connection = playerConnection;
        if (connection != null) connection.setPlayer(this);
    }

    @Override
    public void update(long time) {
        // Process received packets
        interpretPacketQueue();
        // It is possible to be removed during packet processing, if thats the case exit immediately.
        if (isRemoved()) return;

        // Send any available queued chunks
        sendPendingChunks();

        super.update(time); // Super update (item pickup/fire management)

        // Experience orb pickup
        if (experiencePickupCooldown.isReady(time)) {
            experiencePickupCooldown.refreshLastUpdate(time);
            this.instance.getEntityTracker().nearbyEntities(position, expandedBoundingBox.width(),
                    EntityTracker.Target.EXPERIENCE_ORBS, experienceOrb -> {
                        if (!expandedBoundingBox.intersectEntity(position, experienceOrb)) return;
                        final PickupExperienceEvent pickupExperienceEvent = new PickupExperienceEvent(this, experienceOrb);
                        EventDispatcher.callCancellable(pickupExperienceEvent, () -> {
                            short experienceCount = pickupExperienceEvent.getExperienceCount(); // TODO give to player
                            experienceOrb.remove();
                        });
                    });
        }

        // Eating animation
        if (isUsingItem()) {
            final PlayerHand itemUseHand = this.itemUseHand;
            if (itemUseTime > 0 && getCurrentItemUseTime() >= itemUseTime) {
                final ItemStack itemStack = getItemInHand(itemUseHand);
                PlayerFinishItemUseEvent finishUseEvent = new PlayerFinishItemUseEvent(this, itemUseHand, itemStack, itemUseTime);
                EventDispatcher.call(finishUseEvent);

                // Reset client state
                triggerStatus((byte) EntityStatuses.Player.MARK_ITEM_FINISHED);

                // Reset server state
                final boolean isOffHand = itemUseHand == PlayerHand.OFF;
                refreshActiveHand(false, isOffHand, finishUseEvent.isRiptideSpinAttack());
                clearItemUse();

                // The client has predicted that the itemstack will have its count reduced, if the server
                // has not changed the item (the default behavior) we need to refresh the slot.
                if (itemStack.equals(getItemInHand(itemUseHand))) {
                    final int slot = isOffHand ? PlayerInventoryUtils.OFFHAND_SLOT : getHeldSlot();
                    inventory.sendSlotRefresh(slot, itemStack);
                }
            }
        }

        updatePose();

        // Tick event
        EventDispatcher.call(new PlayerTickEvent(this));
    }

    @Override
    public void kill() {
        if (!isDead()) {

            Component deathText;
            Component chatMessage;

            // get death screen text to the killed player
            {
                if (lastDamage != null) {
                    deathText = lastDamage.buildDeathScreenText(this);
                } else { // may happen if killed by the server without applying damage
                    deathText = Component.text("Killed by poor programming.");
                }
            }

            // get death message to chat
            {
                if (lastDamage != null) {
                    chatMessage = lastDamage.buildDeathMessage(this);
                } else { // may happen if killed by the server without applying damage
                    chatMessage = Component.text(getUsername() + " was killed by poor programming.");
                }
            }

            // Call player death event
            PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent(this, deathText, chatMessage);
            EventDispatcher.call(playerDeathEvent);

            deathText = playerDeathEvent.getDeathText();
            chatMessage = playerDeathEvent.getChatMessage();

            // #buildDeathScreenText can return null, check here
            if (deathText != null) {
                sendPacket(new DeathCombatEventPacket(getEntityId(), deathText));
            }

            // #buildDeathMessage can return null, check here
            if (chatMessage != null) {
                Audiences.players().sendMessage(chatMessage);
            }

            // Set death location
            if (getInstance() != null)
                setDeathLocation(getInstance().getDimensionName(), getPosition());
        }
        super.kill();
    }

    /**
     * Respawns the player by sending a {@link RespawnPacket} to the player and teleporting him
     * to {@link #getRespawnPoint()}. It also resets fire and health.
     */
    public void respawn() {
        if (!isDead())
            return;

        setFireTicks(0);
        entityMeta.setOnFire(false);
        refreshHealth();

        sendPacket(new RespawnPacket(dimensionTypeId, instance.getDimensionName(),
                0, gameMode, gameMode, false, levelFlat,
                deathLocation, portalCooldown, (byte) RespawnPacket.COPY_ALL,
                DEFAULT_SEA_LEVEL));
        refreshClientStateAfterRespawn();

        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this);
        EventDispatcher.call(respawnEvent);
        refreshIsDead(false);
        updatePose();

        Pos respawnPosition = respawnEvent.getRespawnPosition();

        // The client unloads chunks when respawning, so resend all chunks next to spawn
        ChunkRange.chunksInRange(respawnPosition, settings.effectiveViewDistance(), chunkAdder);
        chunksLoadedByClient = new Vec(respawnPosition.chunkX(), respawnPosition.chunkZ());
        // Client also needs all entities resent to them, since those are unloaded as well
        this.instance.getEntityTracker().nearbyEntitiesByChunkRange(respawnPosition, settings.effectiveViewDistance(),
                EntityTracker.Target.ENTITIES, entity -> {
                    // Skip refreshing self with a new viewer
                    if (!entity.getUuid().equals(getUuid()) && entity.isViewer(this)) {
                        entity.updateNewViewer(this);
                    }
                });
        teleport(respawnPosition).thenRun(this::refreshAfterTeleport);
    }

    /**
     * Sends necessary packets to synchronize player data after a {@link RespawnPacket}
     */
    private void refreshClientStateAfterRespawn() {
        sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0));
        sendPacket(new ServerDifficultyPacket(MinecraftServer.getDifficulty(), false));
        sendPacket(new UpdateHealthPacket(this.getHealth(), food, foodSaturation));
        sendPacket(new SetExperiencePacket(exp, level, 0));
        triggerStatus((byte) (EntityStatuses.Player.PERMISSION_LEVEL_0 + permissionLevel)); // Set permission level
        refreshAbilities();
    }

    /**
     * Refreshes the command list for this player. This checks the
     * {@link net.minestom.server.command.builder.condition.CommandCondition}s
     * again, and any changes will be visible to the player.
     */
    public void refreshCommands() {
        sendPacket(MinecraftServer.getCommandManager().createDeclareCommandsPacket(this));
    }

    /**
     * Refreshes the recipes and recipe book for this player, testing recipe predicates again.
     */
    public void refreshRecipes() {
        RecipeManager recipeManager = MinecraftServer.getRecipeManager();
        sendPackets(
                recipeManager.getDeclareRecipesPacket(),
                recipeManager.createRecipeBookResetPacket(this)
        );
    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void remove(boolean permanent) {
        if (isRemoved()) return;

        if (permanent) {
            this.packets.clear();
            EventDispatcher.call(new PlayerDisconnectEvent(this));
        }

        super.remove(permanent);

        final AbstractInventory currentInventory = getOpenInventory();
        if (currentInventory != null) currentInventory.removeViewer(this);

        MinecraftServer.getBossBarManager().removeAllBossBars(this);
        // Advancement tabs cache
        {
            Set<AdvancementTab> advancementTabs = AdvancementTab.getTabs(this);
            if (advancementTabs != null) {
                for (AdvancementTab advancementTab : advancementTabs) {
                    advancementTab.removeViewer(this);
                }
            }
        }
        final Pos position = this.position;
        final int chunkX = position.chunkX();
        final int chunkZ = position.chunkZ();
        // Clear all viewable chunks
        ChunkRange.chunksInRange(chunkX, chunkZ, settings.effectiveViewDistance(), chunkRemover);
        // Remove from the tab-list
        PacketSendingUtils.broadcastPlayPacket(getRemovePlayerToList());

        // Prevent the player from being stuck in loading screen, or just unable to interact with the server
        // This should be considered as a bug, since the player will ultimately time out anyway.
        if (permanent && playerConnection.isOnline()) kick(REMOVE_MESSAGE);
    }

    @Override
    public void sendPacketToViewersAndSelf(@NotNull SendablePacket packet) {
        sendPacket(packet);
        super.sendPacketToViewersAndSelf(packet);
    }

    /**
     * Changes the player instance and load surrounding chunks if needed.
     * <p>
     * Be aware that because chunk operations are expensive,
     * it is possible for this method to be non-blocking when retrieving chunks is required.
     *
     * @param instance      the new player instance
     * @param spawnPosition the new position of the player
     * @return a future called once the player instance changed
     */
    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        final Instance currentInstance = this.instance;
        Check.argCondition(currentInstance == instance, "Instance should be different than the current one");
        if (SharedInstance.areLinked(currentInstance, instance) && spawnPosition.sameChunk(this.position)) {
            // The player already has the good version of all the chunks.
            // We just need to refresh his entity viewing list and add him to the instance
            spawnPlayer(instance, spawnPosition, false, false, false);
            return AsyncUtils.VOID_FUTURE;
        }
        // Must update the player chunks
        chunkUpdateLimitChecker.clearHistory();
        final boolean dimensionChange = currentInstance != null && !Objects.equals(currentInstance.getDimensionName(), instance.getDimensionName());
        final Consumer<Instance> runnable = (i) -> spawnPlayer(i, spawnPosition,
                currentInstance == null, dimensionChange, true);

        // Reset chunk queue state
        needsChunkPositionSync = true;
        targetChunksPerTick = 9f;
        pendingChunkCount = 0f;

        // Ensure that surrounding chunks are loaded
        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        ChunkRange.chunksInRange(spawnPosition, settings.effectiveViewDistance(), (chunkX, chunkZ) -> {
            final CompletableFuture<Chunk> future = instance.loadOptionalChunk(chunkX, chunkZ);
            if (!future.isDone()) futures.add(future);
        });
        if (futures.isEmpty()) {
            // All chunks are already loaded
            runnable.accept(instance);
            return AsyncUtils.VOID_FUTURE;
        }

        // One or more chunks need to be loaded
        final Thread runThread = Thread.currentThread();
        CountDownLatch latch = new CountDownLatch(1);
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        CompletableFuture<Void> future = new CompletableFuture<>() {
            @Override
            public Void join() {
                // Prevent deadlock
                if (runThread == Thread.currentThread()) {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    scheduler.process();
                    assert isDone();
                }
                return super.join();
            }
        };

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    scheduler.scheduleNextProcess(() -> {
                        runnable.accept(instance);
                        future.complete(null);
                    });
                    latch.countDown();
                });
        return future;
    }

    /**
     * Changes the player instance without changing its position (defaulted to {@link #getRespawnPoint()}
     * if the player is not in any instance).
     *
     * @param instance the new player instance
     * @return a {@link CompletableFuture} called once the entity's instance has been set,
     * this is due to chunks needing to load for players
     * @see #setInstance(Instance, Pos)
     */
    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance) {
        return setInstance(instance, this.instance != null ? getPosition() : getRespawnPoint());
    }

    /**
     * Used to spawn the player once the client has all the required chunks.
     * <p>
     * Does add the player to {@code instance}, remove all viewable entities and call {@link PlayerSpawnEvent}.
     * <p>
     * UNSAFE: only called with {@link #setInstance(Instance, Pos)}.
     *
     * @param spawnPosition the position to teleport the player
     * @param firstSpawn    true if this is the player first spawn
     * @param updateChunks  true if chunks should be refreshed, false if the new instance shares the same
     *                      chunks
     */
    private void spawnPlayer(@NotNull Instance instance, @NotNull Pos spawnPosition,
                             boolean firstSpawn, boolean dimensionChange, boolean updateChunks) {
        if (!firstSpawn && !dimensionChange) {
            // Player instance changed, clear current viewable collections
            if (updateChunks)
                ChunkRange.chunksInRange(spawnPosition, settings.effectiveViewDistance(), chunkRemover);
        }

        if (dimensionChange) sendDimension(instance.getDimensionType(), instance.getDimensionName());

        super.setInstance(instance, spawnPosition);

        if (updateChunks) {
            final int chunkX = spawnPosition.chunkX();
            final int chunkZ = spawnPosition.chunkZ();
            chunksLoadedByClient = new Vec(chunkX, chunkZ);
            chunkUpdateLimitChecker.addToHistory(getChunk());
            sendPacket(new UpdateViewPositionPacket(chunkX, chunkZ));

            // Load the nearby chunks and queue them to be sent to them
            ChunkRange.chunksInRange(spawnPosition, settings.effectiveViewDistance(), chunkAdder);
            sendPendingChunks(); // Send available first chunk immediately to prevent falling through the floor
        }

        synchronizePositionAfterTeleport(spawnPosition, Vec.ZERO, RelativeFlags.NONE, true); // So the player doesn't get stuck

        if (dimensionChange) {
            sendPacket(new SpawnPositionPacket(spawnPosition, 0));
            sendPacket(instance.createInitializeWorldBorderPacket());
            sendPacket(instance.createTimePacket());
        }

        if (dimensionChange || firstSpawn) {
            this.inventory.update();
            sendPacket(new HeldItemChangePacket(heldSlot));

            // Tell the client to leave the loading terrain screen
            sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0));
        }

        EventDispatcher.call(new PlayerSpawnEvent(this, instance, firstSpawn));
    }

    @ApiStatus.Internal
    public void onChunkBatchReceived(float newTargetChunksPerTick) {
//        logger.debug("chunk batch received player={} chunks/tick={} lead={}", username, newTargetChunksPerTick, chunkBatchLead);
        chunkBatchLead -= 1;
        targetChunksPerTick = Float.isNaN(newTargetChunksPerTick) ? ServerFlag.MIN_CHUNKS_PER_TICK : MathUtils.clamp(
                newTargetChunksPerTick * ServerFlag.CHUNKS_PER_TICK_MULTIPLIER, ServerFlag.MIN_CHUNKS_PER_TICK, ServerFlag.MAX_CHUNKS_PER_TICK);

        // Beyond the first batch we can preemptively send up to 10 (matching mojang server)
        if (maxChunkBatchLead == 1) maxChunkBatchLead = 10;
    }

    /**
     * Queues the given chunk to be sent to the player.
     *
     * @param chunk The chunk to send
     */
    public void sendChunk(@NotNull Chunk chunk) {
        if (!chunk.isLoaded()) return;
        chunkQueueLock.lock();
        try {
            chunkQueue.enqueue(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()));
        } finally {
            chunkQueueLock.unlock();
        }
    }

    private void sendPendingChunks() {
        // If we have nothing to send or have sent the max # of batches without reply, do nothing
        if (chunkQueue.isEmpty() || chunkBatchLead >= maxChunkBatchLead) return;

        // Increment the pending chunk count by the target chunks per tick
        pendingChunkCount = Math.min(pendingChunkCount + targetChunksPerTick, ServerFlag.MAX_CHUNKS_PER_TICK);
        if (pendingChunkCount < 1) return; // Cant send anything

        chunkQueueLock.lock();
        try {
            int batchSize = 0;
            sendPacket(new ChunkBatchStartPacket());
            while (!chunkQueue.isEmpty() && pendingChunkCount >= 1f) {
                long chunkIndex = chunkQueue.dequeueLong();
                int chunkX = CoordConversion.chunkIndexGetX(chunkIndex), chunkZ = CoordConversion.chunkIndexGetZ(chunkIndex);
                var chunk = instance.getChunk(chunkX, chunkZ);
                if (chunk == null || !chunk.isLoaded()) continue;

                sendPacket(chunk.getFullDataPacket());
                EventDispatcher.call(new PlayerChunkLoadEvent(this, chunkX, chunkZ));

                pendingChunkCount -= 1f;
                batchSize += 1;
            }
            sendPacket(new ChunkBatchFinishedPacket(batchSize));
            chunkBatchLead += 1;
//            logger.debug("chunk batch sent player={} chunks={} lead={}", username, batchSize, chunkBatchLead);

            // After sending the first chunk we always send a synchronize position to the client. This is to prevent
            // cases where the client falls through the floor slightly while loading the first chunk.
            // In the vanilla server they have an anticheat which teleports the client back if they enter the floor,
            // but since Minestom does not have an anticheat this provides a similar effect.
            if (needsChunkPositionSync) {
                synchronizePositionAfterTeleport(getPosition(), Vec.ZERO, RelativeFlags.NONE, true);
                needsChunkPositionSync = false;
            }
        } finally {
            chunkQueueLock.unlock();
        }
    }

    @Override
    protected void updatePose() {
        EntityPose oldPose = getPose();
        EntityPose newPose;

        // Figure out their expected state
        var meta = getEntityMeta();
        if (meta.isFlyingWithElytra()) {
            newPose = EntityPose.FALL_FLYING;
        } else if (meta instanceof LivingEntityMeta livingMeta && livingMeta.getBedInWhichSleepingPosition() != null) {
            newPose = EntityPose.SLEEPING;
        } else if (meta.isSwimming()) {
            newPose = EntityPose.SWIMMING;
        } else if (meta instanceof LivingEntityMeta livingMeta && livingMeta.isInRiptideSpinAttack()) {
            newPose = EntityPose.SPIN_ATTACK;
        } else if (isSneaking() && !isFlying()) {
            newPose = EntityPose.SNEAKING;
        } else {
            newPose = EntityPose.STANDING;
        }

        // Try to put them in their expected state, or the closest if they don't fit.
        if (canFitWithBoundingBox(newPose)) {
            // Use expected state
        } else if (canFitWithBoundingBox(EntityPose.SNEAKING)) {
            newPose = EntityPose.SNEAKING;
        } else if (canFitWithBoundingBox(EntityPose.SWIMMING)) {
            newPose = EntityPose.SWIMMING;
        } else {
            // If they can't fit anywhere, just use standing
            newPose = EntityPose.STANDING;
        }

        if (newPose != oldPose) setPose(newPose);
    }

    /**
     * Returns true if the player can fit at the current position with the given {@link EntityPose}, false otherwise.
     *
     * @param pose The pose to check
     */
    private boolean canFitWithBoundingBox(@NotNull EntityPose pose) {
        BoundingBox bb = pose == EntityPose.STANDING ? boundingBox : BoundingBox.fromPose(pose);
        if (bb == null) return false;

        var position = getPosition();
        var iter = bb.getBlocks(getPosition());
        while (iter.hasNext()) {
            var pos = iter.next();
            Block block;
            try {
                block = instance.getBlock(pos.blockX(), pos.blockY(), pos.blockZ(), Block.Getter.Condition.TYPE);
            } catch (NullPointerException ignored) {
                block = null;
            }

            // Block was in unloaded chunk, no bounding box.
            if (block == null) continue;

            // For now just ignore scaffolding. It seems to have a dynamic bounding box, or is just parsed
            // incorrectly in MinestomDataGenerator.
            if (block.id() == Block.SCAFFOLDING.id()) continue;

            var hit = block.registry().collisionShape()
                    .intersectBox(position.sub(pos.blockX(), pos.blockY(), pos.blockZ()), bb);
            if (hit) return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public void sendMessage(final @NotNull Identity source, final @NotNull Component message, final @NotNull MessageType type) {
        // Note to readers: this method may be deprecated, however it is in fact required.
        Messenger.sendMessage(this, message, ChatPosition.fromMessageType(type), source.uuid());
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel the message channel
     * @param data    the message data
     */
    public void sendPluginMessage(@NotNull String channel, byte @NotNull [] data) {
        sendPacket(new PluginMessagePacket(channel, data));
    }

    /**
     * Sends a plugin message to the player.
     * <p>
     * Message encoded to UTF-8.
     *
     * @param channel the message channel
     * @param message the message
     */
    public void sendPluginMessage(@NotNull String channel, @NotNull String message) {
        sendPluginMessage(channel, message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        this.playSound(sound, this.position.x(), this.position.y(), this.position.z());
    }

    public void playSound(@NotNull Sound sound, @NotNull Point point) {
        sendPacket(AdventurePacketConvertor.createSoundPacket(sound, point.x(), point.y(), point.z()));
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        sendPacket(AdventurePacketConvertor.createSoundPacket(sound, x, y, z));
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        final ServerPacket packet;
        if (emitter == Sound.Emitter.self()) {
            packet = AdventurePacketConvertor.createSoundPacket(sound, this);
        } else {
            packet = AdventurePacketConvertor.createSoundPacket(sound, emitter);
        }
        sendPacket(packet);
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        sendPacket(AdventurePacketConvertor.createSoundStopPacket(stop));
    }

    /**
     * Plays a given worldEvent at the given position for this player.
     *
     * @param worldEvent            the worldEvent to play
     * @param x                     x position of the worldEvent
     * @param y                     y position of the worldEvent
     * @param z                     z position of the worldEvent
     * @param data                  data for the worldEvent
     * @param disableRelativeVolume disable volume scaling based on distance
     */
    public void playEffect(@NotNull WorldEvent worldEvent, int x, int y, int z, int data, boolean disableRelativeVolume) {
        sendPacket(new WorldEventPacket(worldEvent.id(), new Vec(x, y, z), data, disableRelativeVolume));
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        sendPacket(new PlayerListHeaderAndFooterPacket(header, footer));
    }

    @Override
    public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        sendPacket(AdventurePacketConvertor.createTitlePartPacket(part, value));
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        sendPacket(new ActionBarPacket(message));
    }

    @Override
    public void resetTitle() {
        sendPacket(new ClearTitlesPacket(true));
    }

    @Override
    public void clearTitle() {
        sendPacket(new ClearTitlesPacket(false));
    }

    @Override
    public void showBossBar(@NotNull BossBar bar) {
        MinecraftServer.getBossBarManager().addBossBar(this, bar);
    }

    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        MinecraftServer.getBossBarManager().removeBossBar(this, bar);
    }

    @Override
    public void openBook(@NotNull Book book) {
        // Close the open inventory if there is one because the book will replace it.
        if (getOpenInventory() != null) {
            closeInventory();
        }

        // TODO: when adventure updates, delete this
        String title = PlainTextComponentSerializer.plainText().serialize(book.title());
        String author = PlainTextComponentSerializer.plainText().serialize(book.author());
        final ItemStack writtenBook = ItemStack.builder(Material.WRITTEN_BOOK)
                .set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(title, author, 0, book.pages(), false))
                .build();

        // Set book in offhand
        sendPacket(new SetSlotPacket((byte) 0, 0, (short) PlayerInventoryUtils.OFFHAND_SLOT, writtenBook));
        // Open the book
        sendPacket(new OpenBookPacket(PlayerHand.OFF));
        // Restore the item in offhand
        sendPacket(new SetSlotPacket((byte) 0, 0, (short) PlayerInventoryUtils.OFFHAND_SLOT, getItemInOffHand()));
    }

    @Override
    public void showDialog(@NotNull DialogLike dialog) {
        sendPacket(new ShowDialogPacket(Dialog.unwrap(dialog)));
    }

    // TODO(1.21.6): Implementation for pending adventure method in 4.24.0.
    public void closeDialog() {
        sendPacket(new ClearDialogPacket());
    }

    @Override
    public void setHealth(float health) {
        sendPacket(new UpdateHealthPacket(health, food, foodSaturation));
        super.setHealth(health);
    }

    /**
     * Gets the entity meta for the player.
     *
     * <p>Note that this method will throw an exception if the player's entity type has
     * been changed with {@link #switchEntityType(EntityType)}. It is wise to check
     * {@link #getEntityType()} first.</p>
     */
    public @NotNull PlayerMeta getPlayerMeta() {
        return (PlayerMeta) super.getEntityMeta();
    }

    /**
     * Gets the player additional hearts.
     *
     * <p>Note that this function is uncallable if the player has their entity type switched
     * with {@link #switchEntityType(EntityType)}.</p>
     *
     * @return the player additional hearts
     */
    public float getAdditionalHearts() {
        return getPlayerMeta().getAdditionalHearts();
    }

    /**
     * Changes the amount of additional hearts shown.
     *
     * <p>Note that this function is uncallable if the player has their entity type switched
     * with {@link #switchEntityType(EntityType)}.</p>
     *
     * @param additionalHearts the count of additional hearts
     */
    public void setAdditionalHearts(float additionalHearts) {
        getPlayerMeta().setAdditionalHearts(additionalHearts);
    }

    /**
     * Gets the player food.
     *
     * @return the player food
     */
    public int getFood() {
        return food;
    }

    /**
     * Sets and refresh client food bar.
     *
     * @param food the new food value
     * @throws IllegalArgumentException if {@code food} is not between 0 and 20
     */
    public void setFood(int food) {
        Check.argCondition(!MathUtils.isBetween(food, 0, 20),
                "Food has to be between 0 and 20");
        this.food = food;
        sendPacket(new UpdateHealthPacket(getHealth(), food, foodSaturation));
    }

    public float getFoodSaturation() {
        return foodSaturation;
    }

    /**
     * Sets and refresh client food saturation.
     *
     * @param foodSaturation the food saturation
     * @throws IllegalArgumentException if {@code foodSaturation} is not between 0 and 20
     */
    public void setFoodSaturation(float foodSaturation) {
        Check.argCondition(!MathUtils.isBetween(foodSaturation, 0, 20),
                "Food saturation has to be between 0 and 20");
        this.foodSaturation = foodSaturation;
        sendPacket(new UpdateHealthPacket(getHealth(), food, foodSaturation));
    }

    /**
     * Gets if the player is eating.
     *
     * @return true if the player is eating, false otherwise
     */
    public boolean isEating() {
        if (!isUsingItem()) return false;
        final ItemStack itemStack = getItemInHand(itemUseHand);
        return itemStack.has(DataComponents.FOOD) || itemStack.material() == Material.POTION;
    }

    /**
     * Gets if the player is using an item.
     *
     * @return true if the player is using an item, false otherwise
     */
    public boolean isUsingItem() {
        return itemUseHand != null;
    }

    /**
     * Gets the hand which the player is using an item from.
     *
     * @return the item use hand, null if none
     */
    public @Nullable PlayerHand getItemUseHand() {
        return itemUseHand;
    }

    /**
     * Gets the amount of ticks which have passed since the player started using an item.
     *
     * @return the amount of ticks which have passed, or zero if the player is not using an item
     */
    public long getCurrentItemUseTime() {
        if (!isUsingItem()) return 0;
        return getAliveTicks() - startItemUseTime;
    }

    @Override
    public double getEyeHeight() {
        return switch (getPose()) {
            case SLEEPING -> 0.2;
            case FALL_FLYING, SWIMMING, SPIN_ATTACK -> 0.4;
            case SNEAKING -> 1.27;
            default -> 1.62;
        };
    }

    /**
     * Gets the player display name in the tab-list.
     *
     * @return the player display name, null means that {@link #getUsername()} is displayed
     */
    public @Nullable Component getDisplayName() {
        return displayName;
    }

    /**
     * Changes the player display name in the tab-list.
     * <p>
     * Sets to null to show the player username.
     *
     * @param displayName the display name, null to display the username
     */
    public void setDisplayName(@Nullable Component displayName) {
        this.displayName = displayName;
        PacketSendingUtils.broadcastPlayPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, infoEntry()));
    }

    /**
     * Gets the player skin.
     *
     * @return the player skin object,
     * null means that the player has his {@link #getUuid()} default skin
     */
    public @Nullable PlayerSkin getSkin() {
        return skin;
    }

    /**
     * Changes the player skin.
     * <p>
     * This does remove the player for all viewers to spawn it again with the correct new skin.
     *
     * @param skin the player skin, null to reset it to his {@link #getUuid()} default skin
     * @see PlayerSkinInitEvent if you want to apply the skin at connection
     */
    public synchronized void setSkin(@Nullable PlayerSkin skin) {
        this.skin = skin;
        if (instance == null)
            return;

        DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket(getEntityId());

        final PlayerInfoRemovePacket removePlayerPacket = getRemovePlayerToList();
        final PlayerInfoUpdatePacket addPlayerPacket = getAddPlayerToList();

        final RespawnPacket respawnPacket = new RespawnPacket(dimensionTypeId,
                instance.getDimensionName(), 0, gameMode, gameMode,
                false, levelFlat, deathLocation, portalCooldown,
                (byte) RespawnPacket.COPY_ALL, DEFAULT_SEA_LEVEL);

        sendPacket(removePlayerPacket);
        sendPacket(destroyEntitiesPacket);
        sendPacket(addPlayerPacket);
        sendPacket(respawnPacket);
        refreshClientStateAfterRespawn();

        {
            // Remove player
            PacketSendingUtils.broadcastPlayPacket(removePlayerPacket);
            sendPacketToViewers(destroyEntitiesPacket);

            // Show player again
            PacketSendingUtils.broadcastPlayPacket(addPlayerPacket);
            getViewers().forEach(player -> showPlayer(player.getPlayerConnection()));
        }

        getInventory().update();
        teleport(getPosition());
    }

    public void setDeathLocation(@NotNull Pos position) {
        setDeathLocation(getInstance().getDimensionName(), position);
    }

    public void setDeathLocation(@NotNull String dimension, @NotNull Pos position) {
        this.deathLocation = new WorldPos(dimension, position);
    }

    public @Nullable WorldPos getDeathLocation() {
        return this.deathLocation;
    }

    /**
     * Gets if the player has the respawn screen enabled or disabled.
     *
     * @return true if the player has the respawn screen, false if he didn't
     */
    public boolean isEnableRespawnScreen() {
        return enableRespawnScreen;
    }

    /**
     * Enables or disable the respawn screen.
     *
     * @param enableRespawnScreen true to enable the respawn screen, false to disable it
     */
    public void setEnableRespawnScreen(boolean enableRespawnScreen) {
        this.enableRespawnScreen = enableRespawnScreen;
        sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.ENABLE_RESPAWN_SCREEN, enableRespawnScreen ? 0 : 1));
    }

    /**
     * Gets the player's name as a component. This will either return the display name
     * (if set) or a component holding the username.
     *
     * @return the name
     */
    @Override
    public @NotNull Component getName() {
        return Objects.requireNonNullElse(displayName, usernameComponent);
    }

    /**
     * Gets the player's username.
     *
     * @return the player's username
     */
    public @NotNull String getUsername() {
        return username;
    }

    /**
     * Calls an {@link ItemDropEvent} with a specified item.
     * <p>
     * Returns false if {@code item} is air.
     *
     * @param item the item to drop
     * @return true if player can drop the item (event not cancelled), false otherwise
     */
    public boolean dropItem(@NotNull ItemStack item) {
        if (item.isAir()) return false;
        ItemDropEvent itemDropEvent = new ItemDropEvent(this, item);
        EventDispatcher.call(itemDropEvent);
        return !itemDropEvent.isCancelled();
    }

    @Override
    public void sendResourcePacks(@NotNull ResourcePackRequest request) {
        if (request.replace()) clearResourcePacks();

        for (final ResourcePackInfo pack : request.packs()) {
            sendPacket(new ResourcePackPushPacket(pack, request.required(), request.prompt()));
            pendingResourcePacks.put(pack.id(), new PendingResourcePack(request.required(), request.callback()));
            if (resourcePackFuture == null) {
                resourcePackFuture = new CompletableFuture<>();
            }
        }
    }

    @Override
    public void removeResourcePacks(@NotNull UUID id, @NotNull UUID @NotNull ... others) {
        sendPacket(new ResourcePackPopPacket(id));
        for (var other : others) {
            sendPacket(new ResourcePackPopPacket(other));
        }
    }

    @Override
    public void clearResourcePacks() {
        sendPacket(new ResourcePackPopPacket((UUID) null));
    }

    /**
     * If there are resource packs in-flight, a future is returned which will be completed when
     * all resource packs have been responded to by the client. Otherwise null is returned.
     */
    @ApiStatus.Internal
    public @Nullable CompletableFuture<Void> getResourcePackFuture() {
        return resourcePackFuture;
    }

    @ApiStatus.Internal
    public void onResourcePackStatus(@NotNull UUID id, @NotNull ResourcePackStatus status) {
        var pendingPack = pendingResourcePacks.get(id);
        if (pendingPack == null) return;

        pendingPack.callback().packEventReceived(id, status, this);
        if (!status.intermediate()) {
            // Remove the callback and finish the future if relevant
            pendingResourcePacks.remove(id);

            // If the resource pack is required and failed to load, bye bye!
            if (pendingPack.required() && status != ResourcePackStatus.SUCCESSFULLY_LOADED) {
                kick(MISSING_REQUIRED_RESOURCE_PACK);
            }

            if (pendingResourcePacks.isEmpty() && resourcePackFuture != null) {
                resourcePackFuture.complete(null);
                resourcePackFuture = null;
            }
        }
    }

    /**
     * Rotates the player to face {@code targetPosition}.
     *
     * @param facePoint      the point from where the player should aim
     * @param targetPosition the target position to face
     */
    public void facePosition(@NotNull FacePoint facePoint, @NotNull Point targetPosition) {
        facePosition(facePoint, targetPosition, null, null);
    }

    /**
     * Rotates the player to face {@code entity}.
     *
     * @param facePoint   the point from where the player should aim
     * @param entity      the entity to face
     * @param targetPoint the point to aim at {@code entity} position
     */
    public void facePosition(@NotNull FacePoint facePoint, Entity entity, FacePoint targetPoint) {
        facePosition(facePoint, entity.getPosition(), entity, targetPoint);
    }

    private void facePosition(@NotNull FacePoint facePoint, @NotNull Point targetPosition,
                              @Nullable Entity entity, @Nullable FacePoint targetPoint) {
        final int entityId = entity != null ? entity.getEntityId() : 0;
        sendPacket(new FacePlayerPacket(
                facePoint == FacePoint.EYE ?
                        FacePlayerPacket.FacePosition.EYES : FacePlayerPacket.FacePosition.FEET, targetPosition,
                entityId,
                targetPoint == FacePoint.EYE ?
                        FacePlayerPacket.FacePosition.EYES : FacePlayerPacket.FacePosition.FEET));
    }

    /**
     * Sets the camera at {@code entity} eyes.
     *
     * @param entity the entity to spectate
     */
    public void spectate(@NotNull Entity entity) {
        sendPacket(new CameraPacket(entity.getEntityId()));
    }

    /**
     * Resets the camera at the player.
     */
    public void stopSpectating() {
        spectate(this);
    }

    /**
     * Used to retrieve the default spawn point.
     * <p>
     * Can be altered by the {@link PlayerRespawnEvent#setRespawnPosition(Pos)}.
     *
     * @return a copy of the default respawn point
     */
    public @NotNull Pos getRespawnPoint() {
        return respawnPoint;
    }

    /**
     * Changes the default spawn point.
     *
     * @param respawnPoint the player respawn point
     */
    public void setRespawnPoint(@NotNull Pos respawnPoint) {
        this.respawnPoint = respawnPoint;
    }

    /**
     * Called after the player teleportation to refresh his position
     * and send data to his new viewers.
     */
    protected void refreshAfterTeleport() {
        sendPacketsToViewers(getSpawnPacket());

        // Update for viewers
        sendPacketToViewersAndSelf(getVelocityPacket());
        sendPacketToViewersAndSelf(getMetadataPacket());
        sendPacketToViewersAndSelf(getPropertiesPacket());
        sendPacketToViewersAndSelf(getEquipmentsPacket());

        getInventory().update();
    }

    /**
     * Sets the player food and health values to their maximum.
     */
    protected void refreshHealth() {
        this.food = 20;
        this.foodSaturation = 5;
        // refresh health and send health packet
        heal();
    }

    /**
     * Gets the percentage displayed in the experience bar.
     *
     * @return the exp percentage 0-1
     */
    public float getExp() {
        return exp;
    }

    /**
     * Used to change the percentage experience bar.
     * This cannot change the displayed level, see {@link #setLevel(int)}.
     *
     * @param exp a percentage between 0 and 1
     * @throws IllegalArgumentException if {@code exp} is not between 0 and 1
     */
    public void setExp(float exp) {
        Check.argCondition(!MathUtils.isBetween(exp, 0, 1), "Exp should be between 0 and 1");
        this.exp = exp;
        sendPacket(new SetExperiencePacket(exp, level, 0));
    }

    /**
     * Gets the level of the player displayed in the experience bar.
     *
     * @return the player level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Used to change the level of the player
     * This cannot change the displayed percentage bar see {@link #setExp(float)}
     *
     * @param level the new level of the player
     */
    public void setLevel(int level) {
        this.level = level;
        sendPacket(new SetExperiencePacket(exp, level, 0));
    }

    public int getPortalCooldown() {
        return portalCooldown;
    }

    public void setPortalCooldown(int portalCooldown) {
        this.portalCooldown = portalCooldown;
    }

    /**
     * Gets the player connection.
     * <p>
     * Used to send packets and get stuff related to the connection.
     *
     * @return the player connection
     */
    public @NotNull PlayerConnection getPlayerConnection() {
        return playerConnection;
    }

    /**
     * Shortcut for {@link PlayerConnection#sendPacket(SendablePacket)}.
     *
     * @param packet the packet to send
     */
    public void sendPacket(@NotNull SendablePacket packet) {
        this.playerConnection.sendPacket(packet);
    }

    public void sendPackets(@NotNull SendablePacket... packets) {
        this.playerConnection.sendPackets(packets);
    }

    public void sendPackets(@NotNull Collection<SendablePacket> packets) {
        this.playerConnection.sendPackets(packets);
    }

    /**
     * Gets if the player is online or not.
     *
     * @return true if the player is online, false otherwise
     */
    public boolean isOnline() {
        return playerConnection.isOnline();
    }

    /**
     * Gets the player settings.
     *
     * @return the player settings
     */
    public @NotNull ClientSettings getSettings() {
        return settings;
    }

    /**
     * Changes the player settings internally.
     * <p>
     * WARNING: the player will not be noticed by this change, probably unsafe.
     */
    public void refreshSettings(ClientSettings settings) {
        final ClientSettings previous = this.settings;
        this.settings = settings;
        boolean isInPlayState = getPlayerConnection().getConnectionState() == ConnectionState.PLAY;
        PlayerMeta playerMeta = getPlayerMeta();
        if (isInPlayState) playerMeta.setNotifyAboutChanges(false);
        playerMeta.setDisplayedSkinParts(settings.displayedSkinParts());
        playerMeta.setRightMainHand(settings.mainHand() == ClientSettings.MainHand.RIGHT);
        if (isInPlayState) playerMeta.setNotifyAboutChanges(true);

        final byte previousViewDistance = previous.viewDistance();
        final byte newViewDistance = settings.viewDistance();
        // Check to see if we're in an instance first, as this method is called when first logging in since the client sends the Settings packet during configuration
        if (instance != null) {
            // Load/unload chunks if necessary due to view distance changes
            if (previousViewDistance < newViewDistance) {
                // View distance expanded, send chunks
                ChunkRange.chunksInRange(position.chunkX(), position.chunkZ(), newViewDistance, (chunkX, chunkZ) -> {
                    if (Math.abs(chunkX - position.chunkX()) > previousViewDistance || Math.abs(chunkZ - position.chunkZ()) > previousViewDistance) {
                        chunkAdder.accept(chunkX, chunkZ);
                    }
                });
            } else if (previousViewDistance > newViewDistance) {
                // View distance shrunk, unload chunks
                ChunkRange.chunksInRange(position.chunkX(), position.chunkZ(), previousViewDistance, (chunkX, chunkZ) -> {
                    if (Math.abs(chunkX - position.chunkX()) > newViewDistance || Math.abs(chunkZ - position.chunkZ()) > newViewDistance) {
                        chunkRemover.accept(chunkX, chunkZ);
                    }
                });
            }
            // Else previous and current are equal, do nothing
        }
    }

    /**
     * Gets the player dimension.
     *
     * @return the player current dimension
     */
    public DimensionType getDimensionType() {
        return DIMENSION_TYPE_REGISTRY.get(dimensionTypeId);
    }

    public @NotNull PlayerInventory getInventory() {
        return inventory;
    }

    /**
     * Used to get the player latency,
     * computed by seeing how long it takes the client to answer the {@link KeepAlivePacket} packet.
     *
     * @return the player latency
     */
    public int getLatency() {
        return latency;
    }

    /**
     * Gets the player {@link GameMode}.
     *
     * @return the player current gamemode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Changes the player {@link GameMode}
     *
     * @param gameMode the new player GameMode
     * @return true if the gamemode was changed successfully, false otherwise (cancelled by event)
     */
    public boolean setGameMode(@NotNull GameMode gameMode) {
        PlayerGameModeChangeEvent playerGameModeChangeEvent = new PlayerGameModeChangeEvent(this, gameMode);
        EventDispatcher.call(playerGameModeChangeEvent);
        if (playerGameModeChangeEvent.isCancelled()) {
            // Abort
            return false;
        }

        gameMode = playerGameModeChangeEvent.getNewGameMode();

        this.gameMode = gameMode;
        // Condition to prevent sending the packets before spawning the player
        if (isActive()) {
            sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, gameMode.ordinal()));
            PacketSendingUtils.broadcastPlayPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, infoEntry()));
        }

        // The client updates their abilities based on the GameMode as follows
        this.allowFlying = gameMode.allowFlying();
        this.instantBreak = gameMode.instantBreak();
        this.invulnerable = gameMode.invulnerable();
        // Spectator automatically enables flying
        // If new game mode cannot fly, disable it
        if (gameMode == GameMode.SPECTATOR || !gameMode.allowFlying()) {
            if (isActive()) {
                refreshFlying(gameMode.allowFlying());
            } else {
                this.flying = gameMode.allowFlying();
            }
        }
        // Make sure that the player is in the PLAY state and synchronize their flight speed.
        if (isActive()) {
            refreshAbilities();
            updateCollisions();
        }

        return true;
    }

    /**
     * Changes the dimension of the player.
     * Mostly unsafe since it requires sending chunks after.
     *
     * @param dimensionType the new player dimension
     */
    protected void sendDimension(@NotNull RegistryKey<DimensionType> dimensionType, @NotNull String dimensionName) {
        Check.argCondition(instance.getDimensionName().equals(dimensionName),
                "The dimension needs to be different than the current one!");
        this.dimensionTypeId = DIMENSION_TYPE_REGISTRY.getId(dimensionType);
        sendPacket(new RespawnPacket(dimensionTypeId, dimensionName,
                0, gameMode, gameMode, false, levelFlat,
                deathLocation, portalCooldown, (byte) RespawnPacket.COPY_ALL,
                DEFAULT_SEA_LEVEL));
        refreshClientStateAfterRespawn();
    }

    /**
     * Kicks the player with a reason.
     *
     * @param component the reason
     */
    public void kick(@NotNull Component component) {
        this.getPlayerConnection().kick(component);
    }

    /**
     * Kicks the player with a reason.
     *
     * @param message the kick reason
     */
    public void kick(@NotNull String message) {
        this.kick(Component.text(message));
    }

    /**
     * Changes the current held slot for the player.
     *
     * @param slot the slot that the player has to held
     * @throws IllegalArgumentException if {@code slot} is not between 0 and 8
     */
    public void setHeldItemSlot(byte slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "Slot has to be between 0 and 8");
        refreshHeldSlot(slot);
        sendPacket(new HeldItemChangePacket(slot));
    }

    /**
     * Gets the player held slot (0-8).
     *
     * @return the current held slot for the player
     */
    public byte getHeldSlot() {
        return heldSlot;
    }

    /**
     * Changes the tag below the name.
     *
     * @param belowNameTag The new below name tag
     */
    public void setBelowNameTag(BelowNameTag belowNameTag) {
        if (this.belowNameTag == belowNameTag) return;

        if (this.belowNameTag != null) {
            this.belowNameTag.removeViewer(this);
        }

        this.belowNameTag = belowNameTag;
    }

    public @NotNull ClickPreprocessor getClickPreprocessor() {
        return clickPreprocessor;
    }

    /**
     * Gets the player open inventory.
     *
     * @return the currently open inventory, null if there is not (player inventory is not detected)
     */
    public @Nullable AbstractInventory getOpenInventory() {
        return openInventory;
    }

    /**
     * Opens the specified Inventory, close the previous inventory if existing.
     *
     * @param inventory the inventory to open
     * @return true if the inventory has been opened/sent to the player, false otherwise (cancelled by event)
     */
    public boolean openInventory(@NotNull Inventory inventory) {
        InventoryOpenEvent inventoryOpenEvent = new InventoryOpenEvent(inventory, this);

        EventDispatcher.callCancellable(inventoryOpenEvent, () -> {
            AbstractInventory openInventory = getOpenInventory();
            if (openInventory != null) {
                openInventory.removeViewer(this);
            }

            AbstractInventory newInventory = inventoryOpenEvent.getInventory();

            newInventory.addViewer(this);
            this.openInventory = newInventory;
        });
        return !inventoryOpenEvent.isCancelled();
    }

    /**
     * Closes the current inventory if there is any.
     * It closes the player inventory (when opened) if {@link #getOpenInventory()} returns null.
     */
    public void closeInventory() {
        closeInventory(false);
    }

    @ApiStatus.Internal
    public void closeInventory(boolean fromClient) {
        AbstractInventory openInventory = getOpenInventory();
        if (openInventory == null) return;

        InventoryCloseEvent inventoryCloseEvent = new InventoryCloseEvent(openInventory, this, fromClient);
        EventDispatcher.call(inventoryCloseEvent);

        if (!fromClient) {
            didCloseInventory = true;
        }

        this.openInventory = null;
        openInventory.removeViewer(this);
        inventory.update();

        didCloseInventory = false;

        Inventory newInventory = inventoryCloseEvent.getNewInventory();
        if (newInventory != null)
            openInventory(newInventory);
    }

    /**
     * Used internally to determine when sending the close inventory packet should be skipped.
     */
    public boolean didCloseInventory() {
        return didCloseInventory;
    }

    /**
     * Used internally to reset the skipClosePacket field, which determines when sending the close inventory packet
     * should be skipped.
     * <p>
     * Shouldn't be used externally without proper understanding of its consequence.
     *
     * @param didCloseInventory the new didCloseInventory field
     */
    @ApiStatus.Internal
    public void UNSAFE_changeDidCloseInventory(boolean didCloseInventory) {
        this.didCloseInventory = didCloseInventory;
    }

    public int getNextTeleportId() {
        return teleportId.incrementAndGet();
    }

    public int getLastSentTeleportId() {
        return teleportId.get();
    }

    public int getLastReceivedTeleportId() {
        return receivedTeleportId;
    }

    public void refreshReceivedTeleportId(int receivedTeleportId) {
        if (receivedTeleportId < 0) return;
        this.receivedTeleportId = receivedTeleportId;
    }

    /**
     * Used to synchronize player position with viewers on spawn or after {@link Entity#teleport(Pos, long[], int)}
     * in properties where a {@link PlayerPositionAndLookPacket} is required
     *
     * @param position      the position used by {@link PlayerPositionAndLookPacket}
     *                      this may not be the same as the {@link Entity#position}
     * @param relativeFlags byte flags used by {@link PlayerPositionAndLookPacket}
     * @param shouldConfirm if false, the teleportation will be done without confirmation
     */
    @ApiStatus.Internal
    void synchronizePositionAfterTeleport(@NotNull Pos position, @NotNull Point velocity,
                                          @MagicConstant(flagsFromClass = RelativeFlags.class) int relativeFlags,
                                          boolean shouldConfirm) {
        int teleportId = shouldConfirm ? getNextTeleportId() : -1;
        sendPacket(new PlayerPositionAndLookPacket(teleportId, position, velocity, position.yaw(), position.pitch(), relativeFlags));
        super.synchronizePosition();
    }

    /**
     * Forces the player's client to look towards the target yaw/pitch
     *
     * @param yaw   the new yaw
     * @param pitch the new pitch
     */
    @Override
    public void setView(float yaw, float pitch) {
        teleport(new Pos(0, 0, 0, yaw, pitch), null, RelativeFlags.COORD).join();
    }

    /**
     * Forces the player's client to look towards the specified point
     * <p>
     * Note: the player's position is not updated on the server until
     * the client receives this packet
     *
     * @param point the point to look at
     */
    @Override
    public void lookAt(@NotNull Point point) {
        // Let the player's client provide updated position values
        sendPacket(new FacePlayerPacket(FacePlayerPacket.FacePosition.EYES, point, 0, null));
    }

    /**
     * Forces the player's client to look towards the specified entity
     * <p>
     * Note: the player's position is not updated on the server until
     * the client receives this packet
     *
     * @param entity the entity to look at
     */
    @Override
    public void lookAt(@NotNull Entity entity) {
        // Let the player's client provide updated position values
        sendPacket(new FacePlayerPacket(FacePlayerPacket.FacePosition.EYES, entity.getPosition(), entity.getEntityId(), FacePlayerPacket.FacePosition.EYES));
    }

    /**
     * Gets the player permission level.
     *
     * @return the player permission level
     */
    public int getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * Changes the player permission level.
     *
     * @param permissionLevel the new player permission level
     * @throws IllegalArgumentException if {@code permissionLevel} is not between 0 and 4
     */
    public void setPermissionLevel(int permissionLevel) {
        Check.argCondition(!MathUtils.isBetween(permissionLevel, 0, 4), "permissionLevel has to be between 0 and 4");

        this.permissionLevel = permissionLevel;

        // Condition to prevent sending the packets before spawning the player
        if (isActive()) {

            final byte permissionLevelStatus = (byte) (EntityStatuses.Player.PERMISSION_LEVEL_0 + permissionLevel);
            triggerStatus(permissionLevelStatus);
        }
    }

    /**
     * Sets or remove the reduced debug screen.
     *
     * @param reduced should the player has the reduced debug screen
     */
    public void setReducedDebugScreenInformation(boolean reduced) {
        this.reducedDebugScreenInformation = reduced;

        final byte debugScreenStatus = (byte) (reduced ? EntityStatuses.Player.ENABLE_DEBUG_SCREEN : EntityStatuses.Player.DISABLE_DEBUG_SCREEN);
        triggerStatus(debugScreenStatus);
    }

    /**
     * Gets if the player has the reduced debug screen.
     *
     * @return true if the player has the reduced debug screen, false otherwise
     */
    public boolean hasReducedDebugScreenInformation() {
        return reducedDebugScreenInformation;
    }

    /**
     * This do update the {@code invulnerable} field in the packet {@link PlayerAbilitiesPacket}
     * and prevent the player from receiving damage.
     *
     * @param invulnerable should the player be invulnerable
     */
    public void setInvulnerable(boolean invulnerable) {
        super.setInvulnerable(invulnerable);
        refreshAbilities();
    }

    @Override
    public void setSneaking(boolean sneaking) {
        if (isFlying()) { //If we are flying, don't set the players pose to sneaking as this can clip them through blocks
            this.entityMeta.setSneaking(sneaking);
        } else {
            super.setSneaking(sneaking);
        }
    }

    /**
     * Gets if the player is currently flying.
     *
     * @return true if the player if flying, false otherwise
     */
    public boolean isFlying() {
        return flying;
    }

    /**
     * Sets the player flying.
     *
     * @param flying should the player fly
     */
    public void setFlying(boolean flying) {
        refreshFlying(flying);
        refreshAbilities();
    }

    /**
     * Updates the internal flying field.
     * <p>
     * Mostly unsafe since there is nothing to backup the value, used internally for creative players.
     *
     * @param flying the new flying field
     * @see #setFlying(boolean) instead
     */
    public void refreshFlying(boolean flying) {
        //When the player starts or stops flying, their pose needs to change
        if (this.flying != flying) {
            EntityPose pose = getPose();

            if (this.isSneaking() && pose == EntityPose.STANDING) {
                setPose(EntityPose.SNEAKING);
            } else if (pose == EntityPose.SNEAKING) {
                setPose(EntityPose.STANDING);
            }
        }

        this.flying = flying;
    }

    /**
     * Gets if the player is allowed to fly.
     *
     * @return true if the player if allowed to fly, false otherwise
     */
    public boolean isAllowFlying() {
        return allowFlying;
    }

    /**
     * Allows or forbid the player to fly.
     *
     * @param allowFlying should the player be allowed to fly
     */
    public void setAllowFlying(boolean allowFlying) {
        this.allowFlying = allowFlying;
        refreshAbilities();
    }

    public boolean isInstantBreak() {
        return instantBreak;
    }

    /**
     * Changes the player ability "Creative Mode".
     *
     * @param instantBreak true to allow instant break
     * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Player_Abilities_(clientbound)">player abilities</a>
     */
    public void setInstantBreak(boolean instantBreak) {
        this.instantBreak = instantBreak;
        refreshAbilities();
    }

    /**
     * Gets the player flying speed.
     *
     * @return the flying speed of the player
     */
    public float getFlyingSpeed() {
        return flyingSpeed;
    }

    /**
     * Updates the internal field and send a {@link PlayerAbilitiesPacket} with the new flying speed.
     *
     * @param flyingSpeed the new flying speed of the player
     */
    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
        refreshAbilities();
    }

    public float getFieldViewModifier() {
        return fieldViewModifier;
    }

    public void setFieldViewModifier(float fieldViewModifier) {
        this.fieldViewModifier = fieldViewModifier;
        refreshAbilities();
    }

    /**
     * This is the map used to send the statistic packet.
     * It is possible to add/remove/change statistic value directly into it.
     *
     * @return the modifiable statistic map
     */
    public @NotNull Map<PlayerStatistic, Integer> getStatisticValueMap() {
        return statisticValueMap;
    }

    /**
     * Gets the last reported set of player inputs.
     *
     * <p>This information comes from the client so should be considered as such.</p>
     */
    public @NotNull PlayerInputs inputs() {
        return inputs;
    }

    /**
     * Sends to the player a {@link PlayerAbilitiesPacket} with all the updated fields.
     */
    protected void refreshAbilities() {
        byte flags = 0;
        if (invulnerable)
            flags |= PlayerAbilitiesPacket.FLAG_INVULNERABLE;
        if (flying)
            flags |= PlayerAbilitiesPacket.FLAG_FLYING;
        if (allowFlying)
            flags |= PlayerAbilitiesPacket.FLAG_ALLOW_FLYING;
        if (instantBreak)
            flags |= PlayerAbilitiesPacket.FLAG_INSTANT_BREAK;
        sendPacket(new PlayerAbilitiesPacket(flags, flyingSpeed, fieldViewModifier));
    }

    /**
     * All packets in the queue are executed in the {@link #update(long)} method
     * It is used internally to add all received packet from the client.
     * Could be used to "simulate" a received packet, but to use at your own risk.
     *
     * @param packet the packet to add in the queue
     */
    public void addPacketToQueue(@NotNull ClientPacket packet) {
        final boolean success = packets.offer(packet);
        if (!success) {
            kick(Component.text("Too Many Packets", NamedTextColor.RED));
        }
    }

    @ApiStatus.Internal
    public void interpretPacketQueue() {
        final PacketListenerManager manager = MinecraftServer.getPacketListenerManager();
        // This method is NOT thread-safe
        this.packets.drain(packet -> manager.processClientPacket(packet, playerConnection,
                getPlayerConnection().getConnectionState()), ServerFlag.PLAYER_PACKET_PER_TICK);
    }

    /**
     * Changes the storage player latency and update its tab value.
     *
     * @param latency the new player latency
     */
    public void refreshLatency(int latency) {
        this.latency = latency;
        if (getPlayerConnection().getConnectionState() == ConnectionState.PLAY) {
            PacketSendingUtils.broadcastPlayPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LATENCY, infoEntry()));
        }
    }

    public void refreshOnGround(boolean onGround) {
        this.onGround = onGround;
        if (this.onGround && this.isFlyingWithElytra()) {
            this.setFlyingWithElytra(false);
            EventDispatcher.call(new PlayerStopFlyingWithElytraEvent(this));
        }
    }

    /**
     * Used to change internally the last sent last keep alive id.
     * <p>
     * Warning: could lead to have the player kicked because of a wrong keep alive packet.
     *
     * @param lastKeepAlive the new lastKeepAlive id
     */
    public void refreshKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
        this.answerKeepAlive = false;
    }

    public boolean didAnswerKeepAlive() {
        return answerKeepAlive;
    }

    public void refreshAnswerKeepAlive(boolean answerKeepAlive) {
        this.answerKeepAlive = answerKeepAlive;
    }

    /**
     * Changes the held item for the player viewers
     * Also cancel item usage if {@link #isUsingItem()} was true.
     * <p>
     * Warning: the player will not be noticed by this chance, only his viewers,
     * see instead: {@link #setHeldItemSlot(byte)}.
     *
     * @param slot the new held slot
     */
    public void refreshHeldSlot(byte slot) {
        byte oldHeldSlot = this.heldSlot;
        this.heldSlot = slot;
        syncEquipment(EquipmentSlot.MAIN_HAND);
        updateEquipmentAttributes(inventory.getItemStack(oldHeldSlot), inventory.getItemStack(this.heldSlot), EquipmentSlot.MAIN_HAND);
    }

    public void refreshItemUse(@Nullable PlayerHand itemUseHand, long itemUseTimeTicks) {
        this.itemUseHand = itemUseHand;
        if (itemUseHand != null) {
            this.startItemUseTime = getAliveTicks();
            this.itemUseTime = itemUseTimeTicks;
        } else {
            this.startItemUseTime = 0;
        }
    }

    public void clearItemUse() {
        refreshItemUse(null, 0);
    }

    public void refreshInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
        this.inputs.refresh(forward, backward, left, right, jump, shift, sprint);

        boolean oldSneakingState = isSneaking();
        setSneaking(shift);
        if (oldSneakingState != shift) {
            if (shift) {
                EventDispatcher.call(new PlayerStartSneakingEvent(this));
            } else {
                EventDispatcher.call(new PlayerStopSneakingEvent(this));
            }
        }
    }

    /**
     * Gets the last sent keep alive id.
     *
     * @return the last keep alive id sent to the player
     */
    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    @Override
    public @NotNull HoverEvent<ShowEntity> asHoverEvent(@NotNull UnaryOperator<ShowEntity> op) {
        return HoverEvent.showEntity(ShowEntity.showEntity(EntityType.PLAYER, getUuid(), this.displayName));
    }

    /**
     * Gets the packet to add the player from the tab-list.
     *
     * @return a {@link PlayerInfoUpdatePacket} to add the player
     */
    protected @NotNull PlayerInfoUpdatePacket getAddPlayerToList() {
        return new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                List.of(infoEntry()));
    }

    /**
     * Gets the packet to remove the player from the tab-list.
     *
     * @return a {@link PlayerInfoRemovePacket} to remove the player
     */
    protected @NotNull PlayerInfoRemovePacket getRemovePlayerToList() {
        return new PlayerInfoRemovePacket(getUuid());
    }

    private PlayerInfoUpdatePacket.Entry infoEntry() {
        final PlayerSkin skin = this.skin;
        List<PlayerInfoUpdatePacket.Property> prop = skin != null ?
                List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature())) :
                List.of();
        return new PlayerInfoUpdatePacket.Entry(getUuid(), getUsername(), prop,
                true, getLatency(), getGameMode(), displayName, null, 0);
    }

    /**
     * Sends all the related packet to have the player sent to another with related data
     * (create player, spawn position, velocity, metadata, equipments, passengers, team).
     * <p>
     * WARNING: this alone does not sync the player, please use {@link #addViewer(Player)}.
     *
     * @param connection the connection to show the player to
     */
    protected void showPlayer(@NotNull PlayerConnection connection) {
        connection.sendPacket(getSpawnPacket());
        connection.sendPacket(getVelocityPacket());
        connection.sendPacket(getMetadataPacket());
        connection.sendPacket(getEquipmentsPacket());
        if (hasPassenger()) {
            connection.sendPacket(getPassengersPacket());
        }
        connection.sendPacket(new EntityHeadLookPacket(getEntityId(), position.yaw()));
    }

    @Override
    public @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot) {
        return inventory.getEquipment(slot, heldSlot);
    }

    @Override
    public void setEquipment(@NotNull EquipmentSlot slot, @NotNull ItemStack itemStack) {
        inventory.setEquipment(slot, heldSlot, itemStack);
    }

    @Override
    public @NotNull PlayerSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        final EntitySnapshot snapshot = super.updateSnapshot(updater);
        return new SnapshotImpl.Player(snapshot, username, gameMode);
    }

    public Locale getLocale() {
        return settings.locale();
    }

    /**
     * Sets the player's locale. This will only set the locale of the player as it
     * is stored in the server. This will also be reset if the settings are refreshed.
     *
     * @param locale the new locale
     */
    public void setLocale(@NotNull Locale locale) {
        final ClientSettings settings = this.settings;
        refreshSettings(new ClientSettings(
                locale, settings.viewDistance(), settings.chatMessageType(), settings.chatColors(),
                settings.displayedSkinParts(), settings.mainHand(), settings.enableTextFiltering(),
                settings.allowServerListings(), settings.particleSetting()
        ));
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pointers pointers() {
        return PLAYER_POINTERS_SUPPLIER.view(this);
    }

    @Override
    protected void updateCollisions() {
        preventBlockPlacement = gameMode != GameMode.SPECTATOR;
        collidesWithEntities = gameMode != GameMode.SPECTATOR;
    }

    protected void sendChunkUpdates(Chunk newChunk) {
        if (chunkUpdateLimitChecker.addToHistory(newChunk)) {
            final int newX = newChunk.getChunkX();
            final int newZ = newChunk.getChunkZ();
            final Vec old = chunksLoadedByClient;
            sendPacket(new UpdateViewPositionPacket(newX, newZ));
            ChunkRange.chunksInRangeDiffering(newX, newZ, (int) old.x(), (int) old.z(),
                    settings.effectiveViewDistance(), chunkAdder, chunkRemover);
            this.chunksLoadedByClient = new Vec(newX, newZ);
        }
    }

    /**
     * @see #teleport(Pos, long[], int)
     */
    @Override
    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, long @Nullable [] chunks, int flags) {
        chunkUpdateLimitChecker.clearHistory();
        return super.teleport(position, chunks, flags);
    }

    /**
     * Send a {@link Notification} to the player.
     *
     * @param notification the {@link Notification} to send
     */
    public void sendNotification(@NotNull Notification notification) {
        sendPacket(notification.buildAddPacket());
        sendPacket(notification.buildRemovePacket());
    }

    /**
     * Sends a {@link EntityAnimationPacket} to clear remove the sleep darkness.
     */
    @Override
    public void leaveBed() {
        EntityAnimationPacket packet = new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.LEAVE_BED);
        sendPacket(packet);
        super.leaveBed();
    }

    public enum FacePoint {
        FEET,
        EYE
    }

    // Settings enum

    private int compareChunkDistance(long chunkIndexA, long chunkIndexB) {
        int chunkAX = CoordConversion.chunkIndexGetX(chunkIndexA);
        int chunkAZ = CoordConversion.chunkIndexGetZ(chunkIndexA);
        int chunkBX = CoordConversion.chunkIndexGetX(chunkIndexB);
        int chunkBZ = CoordConversion.chunkIndexGetZ(chunkIndexB);
        int chunkDistanceA = Math.abs(chunkAX - chunksLoadedByClient.blockX()) + Math.abs(chunkAZ - chunksLoadedByClient.blockZ());
        int chunkDistanceB = Math.abs(chunkBX - chunksLoadedByClient.blockX()) + Math.abs(chunkBZ - chunksLoadedByClient.blockZ());
        return Integer.compare(chunkDistanceA, chunkDistanceB);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    @Override
    public @NotNull Acquirable<? extends Player> acquirable() {
        return (Acquirable<? extends Player>) super.acquirable();
    }
}
