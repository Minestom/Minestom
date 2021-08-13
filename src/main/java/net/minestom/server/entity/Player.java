package net.minestom.server.entity;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.AdvancementTab;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.Localizable;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.vehicle.PlayerVehicleInformation;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.item.PickupExperienceEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.WrittenBookMeta;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.resourcepack.ResourcePack;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.statistic.PlayerStatistic;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.TickUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.entity.EntityUtils;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.minestom.server.utils.instance.InstanceUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

/**
 * Those are the major actors of the server,
 * they are not necessary backed by a {@link PlayerSocketConnection} as shown by {@link FakePlayer}.
 * <p>
 * You can easily create your own implementation of this and use it with {@link ConnectionManager#setPlayerProvider(PlayerProvider)}.
 */
public class Player extends LivingEntity implements CommandSender, Localizable, HoverEventSource<ShowEntity>, Identified, NamedAndIdentified {

    private long lastKeepAlive;
    private boolean answerKeepAlive;

    private String username;
    private Component usernameComponent;
    protected final PlayerConnection playerConnection;
    // All the entities that this player can see
    protected final Set<Entity> viewableEntities = ConcurrentHashMap.newKeySet();

    private int latency;
    private Component displayName;
    private PlayerSkin skin;

    private DimensionType dimensionType;
    private GameMode gameMode;
    // Chunks that the player can view
    protected final Set<Chunk> viewableChunks = ConcurrentHashMap.newKeySet();

    private final AtomicInteger teleportId = new AtomicInteger();
    private int receivedTeleportId;

    private final Queue<ClientPlayPacket> packets = new ConcurrentLinkedQueue<>();
    private final boolean levelFlat;
    private final PlayerSettings settings;
    private float exp;
    private int level;

    protected PlayerInventory inventory;
    private Inventory openInventory;
    // Used internally to allow the closing of inventory within the inventory listener
    private boolean didCloseInventory;

    private byte heldSlot;

    private Pos respawnPoint;

    private int food;
    private float foodSaturation;
    private long startEatingTime;
    private long defaultEatingTime = 1000L;
    private long eatingTime;
    private Hand eatingHand;

    // Game state (https://wiki.vg/Protocol#Change_Game_State)
    private boolean enableRespawnScreen;

    // Experience orb pickup
    protected Cooldown experiencePickupCooldown = new Cooldown(Duration.of(10, TimeUnit.SERVER_TICK));

    private BelowNameTag belowNameTag;

    private int permissionLevel;

    private boolean reducedDebugScreenInformation;

    // Abilities
    private boolean flying;
    private boolean allowFlying;
    private boolean instantBreak;
    private float flyingSpeed = 0.05f;
    private float fieldViewModifier = 0.1f;

    // Statistics
    private final Map<PlayerStatistic, Integer> statisticValueMap = new Hashtable<>();

    // Vehicle
    private final PlayerVehicleInformation vehicleInformation = new PlayerVehicleInformation();

    // Tick related
    private final PlayerTickEvent playerTickEvent = new PlayerTickEvent(this);

    // Adventure
    private Identity identity;
    private final Pointers pointers;

    public Player(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(EntityType.PLAYER, uuid);
        this.username = username;
        this.usernameComponent = Component.text(username);
        this.playerConnection = playerConnection;

        setBoundingBox(0.6f, 1.8f, 0.6f);

        setRespawnPoint(Pos.ZERO);

        this.settings = new PlayerSettings();
        this.inventory = new PlayerInventory(this);

        setCanPickupItem(true); // By default

        // Allow the server to send the next keep alive packet
        refreshAnswerKeepAlive(true);

        this.gameMode = GameMode.SURVIVAL;
        this.dimensionType = DimensionType.OVERWORLD; // Default dimension
        this.levelFlat = true;
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1f);

        // FakePlayer init its connection there
        playerConnectionInit();

        this.identity = Identity.identity(uuid);
        this.pointers = Pointers.builder()
                .withDynamic(Identity.UUID, this::getUuid)
                .withDynamic(Identity.NAME, this::getUsername)
                .withDynamic(Identity.DISPLAY_NAME, this::getDisplayName)
                .build();
    }

    /**
     * Used when the player is created.
     * Init the player and spawn him.
     * <p>
     * WARNING: executed in the main update thread
     * UNSAFE: Only meant to be used when a socket player connects through the server.
     *
     * @param spawnInstance the player spawn instance (defined in {@link PlayerLoginEvent})
     */
    public void UNSAFE_init(@NotNull Instance spawnInstance) {
        this.dimensionType = spawnInstance.getDimensionType();

        JoinGamePacket joinGamePacket = new JoinGamePacket();
        joinGamePacket.entityId = getEntityId();
        joinGamePacket.gameMode = gameMode;
        joinGamePacket.dimensionType = dimensionType;
        joinGamePacket.maxPlayers = 0; // Unused
        joinGamePacket.viewDistance = MinecraftServer.getChunkViewDistance();
        joinGamePacket.reducedDebugInfo = false;
        joinGamePacket.isFlat = levelFlat;
        playerConnection.sendPacket(joinGamePacket);

        // Server brand name
        playerConnection.sendPacket(PluginMessagePacket.getBrandPacket());
        // Difficulty
        playerConnection.sendPacket(new ServerDifficultyPacket(MinecraftServer.getDifficulty(), true));

        playerConnection.sendPacket(new SpawnPositionPacket(respawnPoint, 0));

        // Add player to list with spawning skin
        PlayerSkinInitEvent skinInitEvent = new PlayerSkinInitEvent(this, skin);
        EventDispatcher.call(skinInitEvent);
        this.skin = skinInitEvent.getSkin();
        // FIXME: when using Geyser, this line remove the skin of the client
        PacketUtils.broadcastPacket(getAddPlayerToList());
        for (var player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            this.playerConnection.sendPacket(player.getAddPlayerToList());
        }

        // Commands
        refreshCommands();

        // Recipes start
        {
            RecipeManager recipeManager = MinecraftServer.getRecipeManager();
            DeclareRecipesPacket declareRecipesPacket = recipeManager.getDeclareRecipesPacket();
            if (declareRecipesPacket.recipes != null) {
                playerConnection.sendPacket(declareRecipesPacket);
            }

            List<String> recipesIdentifier = new ArrayList<>();
            for (Recipe recipe : recipeManager.getRecipes()) {
                if (!recipe.shouldShow(this))
                    continue;

                recipesIdentifier.add(recipe.getRecipeId());
            }
            if (!recipesIdentifier.isEmpty()) {
                final String[] identifiers = recipesIdentifier.toArray(new String[0]);
                UnlockRecipesPacket unlockRecipesPacket = new UnlockRecipesPacket();
                unlockRecipesPacket.mode = 0;
                unlockRecipesPacket.recipesId = identifiers;
                unlockRecipesPacket.initRecipesId = identifiers;
                playerConnection.sendPacket(unlockRecipesPacket);
            }
        }
        // Recipes end

        // Tags
        this.playerConnection.sendPacket(TagsPacket.getRequiredTagsPacket());

        // Some client updates
        this.playerConnection.sendPacket(getPropertiesPacket()); // Send default properties
        refreshHealth(); // Heal and send health packet
        refreshAbilities(); // Send abilities packet
    }

    /**
     * Used to initialize the player connection
     */
    protected void playerConnectionInit() {
        this.playerConnection.setPlayer(this);
    }

    @Override
    public void update(long time) {
        // Network tick
        this.playerConnection.update();

        // Process received packets
        ClientPlayPacket packet;
        while ((packet = packets.poll()) != null) {
            packet.process(this);
        }

        super.update(time); // Super update (item pickup/fire management)

        // Experience orb pickup
        if (experiencePickupCooldown.isReady(time)) {
            experiencePickupCooldown.refreshLastUpdate(time);
            final Chunk chunk = getChunk(); // TODO check surrounding chunks
            final Set<Entity> entities = instance.getChunkEntities(chunk);
            for (Entity entity : entities) {
                if (entity instanceof ExperienceOrb) {
                    final ExperienceOrb experienceOrb = (ExperienceOrb) entity;
                    final BoundingBox itemBoundingBox = experienceOrb.getBoundingBox();
                    if (expandedBoundingBox.intersect(itemBoundingBox)) {
                        if (experienceOrb.shouldRemove() || experienceOrb.isRemoveScheduled())
                            continue;
                        PickupExperienceEvent pickupExperienceEvent = new PickupExperienceEvent(experienceOrb);
                        EventDispatcher.callCancellable(pickupExperienceEvent, () -> {
                            short experienceCount = pickupExperienceEvent.getExperienceCount(); // TODO give to player
                            entity.remove();
                        });
                    }
                }
            }
        }

        // Eating animation
        if (isEating()) {
            if (time - startEatingTime >= eatingTime) {
                triggerStatus((byte) 9); // Mark item use as finished
                ItemUpdateStateEvent itemUpdateStateEvent = callItemUpdateStateEvent(eatingHand);

                Check.notNull(itemUpdateStateEvent, "#callItemUpdateStateEvent returned null.");

                // Refresh hand
                final boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
                refreshActiveHand(false, isOffHand, false);

                final ItemStack foodItem = itemUpdateStateEvent.getItemStack();
                final boolean isFood = foodItem.getMaterial().isFood();

                if (isFood) {
                    PlayerEatEvent playerEatEvent = new PlayerEatEvent(this, foodItem, eatingHand);
                    EventDispatcher.call(playerEatEvent);
                }

                refreshEating(null);
            }
        }

        // Tick event
        EventDispatcher.call(playerTickEvent);
    }

    @Override
    public void kill() {
        if (!isDead()) {

            Component deathText;
            Component chatMessage;

            // get death screen text to the killed player
            {
                if (lastDamageSource != null) {
                    deathText = lastDamageSource.buildDeathScreenText(this);
                } else { // may happen if killed by the server without applying damage
                    deathText = Component.text("Killed by poor programming.");
                }
            }

            // get death message to chat
            {
                if (lastDamageSource != null) {
                    chatMessage = lastDamageSource.buildDeathMessage(this);
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
                playerConnection.sendPacket(DeathCombatEventPacket.of(getEntityId(), -1, deathText));
            }

            // #buildDeathMessage can return null, check here
            if (chatMessage != null) {
                Audiences.players().sendMessage(chatMessage);
            }

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

        setFireForDuration(0);
        setOnFire(false);
        refreshHealth();
        RespawnPacket respawnPacket = new RespawnPacket();
        respawnPacket.dimensionType = getDimensionType();
        respawnPacket.gameMode = getGameMode();
        respawnPacket.isFlat = levelFlat;
        getPlayerConnection().sendPacket(respawnPacket);
        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this);
        EventDispatcher.call(respawnEvent);
        refreshIsDead(false);

        // Runnable called when teleportation is successful (after loading and sending necessary chunk)
        teleport(respawnEvent.getRespawnPosition()).thenRun(this::refreshAfterTeleport);
    }
    
    /**
     * Refreshes the command list for this player. This checks the
     * {@link net.minestom.server.command.builder.condition.CommandCondition}s
     * again, and any changes will be visible to the player.
     */
    public void refreshCommands() {
        CommandManager commandManager = MinecraftServer.getCommandManager();
        DeclareCommandsPacket declareCommandsPacket = commandManager.createDeclareCommandsPacket(this);
        playerConnection.sendPacket(declareCommandsPacket);
    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void remove() {
        if (isRemoved()) return;
        EventDispatcher.call(new PlayerDisconnectEvent(this));
        super.remove();
        this.packets.clear();
        if (getOpenInventory() != null) {
            getOpenInventory().removeViewer(this);
        }
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
        // Clear all viewable entities
        this.viewableEntities.forEach(entity -> entity.removeViewer(this));
        // Clear all viewable chunks
        this.viewableChunks.forEach(chunk -> chunk.removeViewer(this));
        // Remove from the tab-list
        PacketUtils.broadcastPacket(getRemovePlayerToList());
    }

    @Override
    protected boolean removeViewer0(@NotNull Player player) {
        if (player == this || !super.removeViewer0(player)) {
            return false;
        }
        // Team
        if (this.getTeam() != null && this.getTeam().getMembers().size() == 1) {// If team only contains "this" player
            player.getPlayerConnection().sendPacket(this.getTeam().createTeamDestructionPacket());
        }
        return true;
    }

    @Override
    public void sendPacketToViewersAndSelf(@NotNull ServerPacket packet) {
        this.playerConnection.sendPacket(packet);
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
        Check.argCondition(this.instance == instance, "Instance should be different than the current one");
        // true if the chunks need to be sent to the client, can be false if the instances share the same chunks (e.g. SharedInstance)
        if (!InstanceUtils.areLinked(this.instance, instance) || !spawnPosition.sameChunk(this.position)) {
            return instance.loadOptionalChunk(spawnPosition)
                    .thenRun(() -> spawnPlayer(instance, spawnPosition,
                            this.instance == null,
                            !Objects.equals(dimensionType, instance.getDimensionType()), true));
        } else {
            // The player already has the good version of all the chunks.
            // We just need to refresh his entity viewing list and add him to the instance
            return AsyncUtils.VOID_FUTURE
                    .thenRun(() -> spawnPlayer(instance, spawnPosition, false, false, false));
        }
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
        if (!firstSpawn) {
            // Player instance changed, clear current viewable collections
            this.viewableChunks.forEach(chunk -> chunk.removeViewer(this));
            this.viewableEntities.forEach(entity -> entity.removeViewer(this));
        }

        super.setInstance(instance, spawnPosition);

        if (dimensionChange) {
            sendDimension(instance.getDimensionType());
        }

        if (updateChunks) {
            // Warning: loop to remove once `refreshVisibleChunks` manage it
            this.viewableChunks.forEach(chunk ->
                    playerConnection.sendPacket(new UnloadChunkPacket(chunk.getChunkX(), chunk.getChunkZ())));
            refreshVisibleChunks();
        }

        if (dimensionChange || firstSpawn) {
            synchronizePosition(true); // So the player doesn't get stuck
            this.inventory.update();
        }

        PlayerSpawnEvent spawnEvent = new PlayerSpawnEvent(this, instance, firstSpawn);
        EventDispatcher.call(spawnEvent);
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel the message channel
     * @param data    the message data
     */
    public void sendPluginMessage(@NotNull String channel, byte @NotNull [] data) {
        playerConnection.sendPacket(new PluginMessagePacket(channel, data));
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

    /**
     * @deprecated Use {@link #sendMessage(Component)}
     */
    @Deprecated
    public void sendJsonMessage(@NotNull String json) {
        this.sendMessage(GsonComponentSerializer.gson().deserialize(json));
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        Messenger.sendMessage(this, message, ChatPosition.fromMessageType(type), source.uuid());
    }

    /**
     * Makes the player send a message (can be used for commands).
     *
     * @param message the message that the player will send
     */
    public void chat(@NotNull String message) {
        ClientChatMessagePacket chatMessagePacket = new ClientChatMessagePacket();
        chatMessagePacket.message = message;
        addPacketToQueue(chatMessagePacket);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        this.playSound(sound, this.position.x(), this.position.y(), this.position.z());
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        playerConnection.sendPacket(AdventurePacketConvertor.createSoundPacket(sound, x, y, z));
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        final ServerPacket packet;

        if (emitter == Sound.Emitter.self()) {
            packet = AdventurePacketConvertor.createSoundPacket(sound, this);
        } else {
            packet = AdventurePacketConvertor.createSoundPacket(sound, emitter);
        }

        playerConnection.sendPacket(packet);
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        playerConnection.sendPacket(AdventurePacketConvertor.createSoundStopPacket(stop));
    }

    /**
     * Plays a given effect at the given position for this player.
     *
     * @param effect                the effect to play
     * @param x                     x position of the effect
     * @param y                     y position of the effect
     * @param z                     z position of the effect
     * @param data                  data for the effect
     * @param disableRelativeVolume disable volume scaling based on distance
     */
    public void playEffect(@NotNull Effects effect, int x, int y, int z, int data, boolean disableRelativeVolume) {
        EffectPacket packet = new EffectPacket();
        packet.effectId = effect.getId();
        packet.position = new Vec(x, y, z);
        packet.data = data;
        packet.disableRelativeVolume = disableRelativeVolume;
        playerConnection.sendPacket(packet);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        playerConnection.sendPacket(new PlayerListHeaderAndFooterPacket(header, footer));
    }

    @Override
    public void showTitle(@NotNull Title title) {
        playerConnection.sendPacket(new SetTitleTextPacket(title.title()));
        playerConnection.sendPacket(new SetTitleSubTitlePacket(title.subtitle()));
        final var times = title.times();
        if (times != null) {
            playerConnection.sendPacket(new SetTitleTimePacket(
                    TickUtils.fromDuration(times.fadeIn(), TickUtils.CLIENT_TICK_MS),
                    TickUtils.fromDuration(times.stay(), TickUtils.CLIENT_TICK_MS),
                    TickUtils.fromDuration(times.fadeOut(), TickUtils.CLIENT_TICK_MS)));
        }
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        playerConnection.sendPacket(new ActionBarPacket(message));
    }

    /**
     * Specifies the display time of a title.
     *
     * @param fadeIn  ticks to spend fading in
     * @param stay    ticks to keep the title displayed
     * @param fadeOut ticks to spend out, not when to start fading out
     * @deprecated Use {@link #showTitle(Title)}. Note that this will overwrite the
     * existing title. This is expected behavior and will be the case in 1.17.
     */
    @Deprecated
    public void sendTitleTime(int fadeIn, int stay, int fadeOut) {
        playerConnection.sendPacket(new SetTitleTimePacket(fadeIn, stay, fadeOut));
    }

    @Override
    public void resetTitle() {
        playerConnection.sendPacket(new ClearTitlesPacket(true));
    }

    @Override
    public void clearTitle() {
        playerConnection.sendPacket(new ClearTitlesPacket());
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
        final ItemStack writtenBook = ItemStack.builder(Material.WRITTEN_BOOK)
                .meta(WrittenBookMeta.fromAdventure(book, this))
                .build();
        // Set book in offhand
        playerConnection.sendPacket(new SetSlotPacket((byte) 0, 0, (short) PlayerInventoryUtils.OFFHAND_SLOT, writtenBook));
        // Open the book
        playerConnection.sendPacket(new OpenBookPacket(Hand.OFF));
        // Restore the item in offhand
        playerConnection.sendPacket(new SetSlotPacket((byte) 0, 0, (short) PlayerInventoryUtils.OFFHAND_SLOT, getItemInOffHand()));
    }

    @Override
    public boolean isImmune(@NotNull DamageType type) {
        if (!getGameMode().canTakeDamage()) {
            return type != DamageType.VOID;
        }
        return super.isImmune(type);
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        this.playerConnection.sendPacket(new UpdateHealthPacket(health, food, foodSaturation));
    }

    @Override
    public @NotNull PlayerMeta getEntityMeta() {
        return (PlayerMeta) super.getEntityMeta();
    }

    /**
     * Gets the player additional hearts.
     *
     * @return the player additional hearts
     */
    public float getAdditionalHearts() {
        return getEntityMeta().getAdditionalHearts();
    }

    /**
     * Changes the amount of additional hearts shown.
     *
     * @param additionalHearts the count of additional hearts
     */
    public void setAdditionalHearts(float additionalHearts) {
        getEntityMeta().setAdditionalHearts(additionalHearts);
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
        this.playerConnection.sendPacket(new UpdateHealthPacket(getHealth(), food, foodSaturation));
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
        this.playerConnection.sendPacket(new UpdateHealthPacket(getHealth(), food, foodSaturation));
    }

    /**
     * Gets if the player is eating.
     *
     * @return true if the player is eating, false otherwise
     */
    public boolean isEating() {
        return eatingHand != null;
    }

    /**
     * Gets the hand which the player is eating from.
     *
     * @return the eating hand, null if none
     */
    public @Nullable Hand getEatingHand() {
        return eatingHand;
    }

    /**
     * Gets the player default eating time.
     *
     * @return the player default eating time
     */
    public long getDefaultEatingTime() {
        return defaultEatingTime;
    }

    /**
     * Used to change the default eating time animation.
     *
     * @param defaultEatingTime the default eating time in milliseconds
     */
    public void setDefaultEatingTime(long defaultEatingTime) {
        this.defaultEatingTime = defaultEatingTime;
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

        PlayerInfoPacket infoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME);
        infoPacket.playerInfos.add(new PlayerInfoPacket.UpdateDisplayName(getUuid(), displayName));
        sendPacketToViewersAndSelf(infoPacket);
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

        final PlayerInfoPacket removePlayerPacket = getRemovePlayerToList();
        final PlayerInfoPacket addPlayerPacket = getAddPlayerToList();

        RespawnPacket respawnPacket = new RespawnPacket();
        respawnPacket.dimensionType = getDimensionType();
        respawnPacket.gameMode = getGameMode();
        respawnPacket.isFlat = levelFlat;

        playerConnection.sendPacket(removePlayerPacket);
        playerConnection.sendPacket(destroyEntitiesPacket);
        playerConnection.sendPacket(respawnPacket);
        playerConnection.sendPacket(addPlayerPacket);

        {
            // Remove player
            sendPacketToViewers(removePlayerPacket);
            sendPacketToViewers(destroyEntitiesPacket);

            // Show player again
            getViewers().forEach(player -> showPlayer(player.getPlayerConnection()));
        }

        getInventory().update();
        teleport(getPosition());
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
        sendChangeGameStatePacket(ChangeGameStatePacket.Reason.ENABLE_RESPAWN_SCREEN, enableRespawnScreen ? 0 : 1);
    }

    /**
     * Gets the player's name as a component. This will either return the display name
     * (if set) or a component holding the username.
     *
     * @return the name
     */
    @Override
    public @NotNull Component getName() {
        if (this.displayName != null) {
            return this.displayName;
        } else {
            return this.usernameComponent;
        }
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
     * Changes the internal player name, used for the {@link AsyncPlayerPreLoginEvent}
     * mostly unsafe outside of it.
     *
     * @param username the new player name
     */
    public void setUsernameField(@NotNull String username) {
        this.username = username;
        this.usernameComponent = Component.text(username);
    }

    private void sendChangeGameStatePacket(@NotNull ChangeGameStatePacket.Reason reason, float value) {
        ChangeGameStatePacket changeGameStatePacket = new ChangeGameStatePacket();
        changeGameStatePacket.reason = reason;
        changeGameStatePacket.value = value;
        playerConnection.sendPacket(changeGameStatePacket);
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
        if (item.isAir()) {
            return false;
        }

        ItemDropEvent itemDropEvent = new ItemDropEvent(this, item);
        EventDispatcher.call(itemDropEvent);
        return !itemDropEvent.isCancelled();
    }

    /**
     * Sets the player resource pack.
     *
     * @param resourcePack the resource pack
     */
    public void setResourcePack(@NotNull ResourcePack resourcePack) {
        playerConnection.sendPacket(new ResourcePackSendPacket(resourcePack));
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
        FacePlayerPacket facePlayerPacket = new FacePlayerPacket();
        facePlayerPacket.entityFacePosition = facePoint == FacePoint.EYE ?
                FacePlayerPacket.FacePosition.EYES : FacePlayerPacket.FacePosition.FEET;
        facePlayerPacket.targetX = targetPosition.x();
        facePlayerPacket.targetY = targetPosition.y();
        facePlayerPacket.targetZ = targetPosition.z();
        if (entity != null) {
            facePlayerPacket.entityId = entity.getEntityId();
            facePlayerPacket.entityFacePosition = targetPoint == FacePoint.EYE ?
                    FacePlayerPacket.FacePosition.EYES : FacePlayerPacket.FacePosition.FEET;
        }
        playerConnection.sendPacket(facePlayerPacket);
    }

    /**
     * Sets the camera at {@code entity} eyes.
     *
     * @param entity the entity to spectate
     */
    public void spectate(@NotNull Entity entity) {
        playerConnection.sendPacket(new CameraPacket(entity));
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
        sendPacketsToViewers(getEntityType().registry().spawnType().getSpawnPacket(this));

        // Update for viewers
        sendPacketToViewersAndSelf(getVelocityPacket());
        sendPacketToViewersAndSelf(getMetadataPacket());
        sendPacketToViewersAndSelf(getPropertiesPacket());
        sendPacketToViewersAndSelf(getEquipmentsPacket());

        getInventory().update();

        {
            // Send new chunks
            final Chunk chunk = instance.getChunkAt(position);
            Check.notNull(chunk, "Tried to interact with an unloaded chunk.");
            refreshVisibleChunks(chunk);
        }
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
        this.playerConnection.sendPacket(new SetExperiencePacket(exp, level, 0));
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
        this.playerConnection.sendPacket(new SetExperiencePacket(exp, level, 0));
    }

    /**
     * Called when the player changes chunk (move from one to another).
     * Can also be used to refresh the list of chunks that the client should see based on {@link #getChunkRange()}.
     * <p>
     * It does remove and add the player from the chunks viewers list when removed or added.
     * It also calls the events {@link PlayerChunkUnloadEvent} and {@link PlayerChunkLoadEvent}.
     *
     * @param newChunk the current/new player chunk (can be the current one)
     */
    public void refreshVisibleChunks(@NotNull Chunk newChunk) {
        // Previous chunks indexes
        final long[] lastVisibleChunks = viewableChunks.stream().mapToLong(ChunkUtils::getChunkIndex).toArray();
        // New chunks indexes
        final long[] updatedVisibleChunks = ChunkUtils.getChunksInRange(newChunk.toPosition(), getChunkRange());

        // Update client render distance
        updateViewPosition(newChunk.getChunkX(), newChunk.getChunkZ());

        // Unload old chunks
        ArrayUtils.forDifferencesBetweenArray(lastVisibleChunks, updatedVisibleChunks, chunkIndex -> {
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
            //playerConnection.sendPacket(new UnloadChunkPacket(chunkX, chunkZ));
            final Chunk chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk != null) {
                chunk.removeViewer(this);
            }
        });
        // Load new chunks
        ArrayUtils.forDifferencesBetweenArray(updatedVisibleChunks, lastVisibleChunks, chunkIndex -> {
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
            this.instance.loadOptionalChunk(chunkX, chunkZ).thenAccept(chunk -> {
                if (chunk == null) {
                    // Cannot load chunk (auto load is not enabled)
                    return;
                }
                chunk.addViewer(this);
            });
        });
    }

    public void refreshVisibleChunks() {
        final Chunk chunk = getChunk();
        if (chunk != null) {
            refreshVisibleChunks(chunk);
        }
    }

    /**
     * Refreshes the list of entities that the player should be able to see based
     * on {@link MinecraftServer#getEntityViewDistance()} and {@link Entity#isAutoViewable()}.
     *
     * @param newChunk the new chunk of the player (can be the current one)
     */
    public void refreshVisibleEntities(@NotNull Chunk newChunk) {
        final int entityViewDistance = MinecraftServer.getEntityViewDistance();
        final float maximalDistance = entityViewDistance * Chunk.CHUNK_SECTION_SIZE;
        // Manage already viewable entities
        this.viewableEntities.stream()
                .filter(entity -> entity.getDistance(this) > maximalDistance)
                .forEach(entity -> {
                    // Entity shouldn't be viewable anymore
                    if (isAutoViewable()) {
                        entity.removeViewer(this);
                    }
                    if (entity instanceof Player && entity.isAutoViewable()) {
                        removeViewer((Player) entity);
                    }
                });
        // Manage entities in unchecked chunks
        EntityUtils.forEachRange(instance, newChunk.toPosition(), entityViewDistance, entity -> {
            if (entity.isAutoViewable() && !entity.viewers.contains(this)) {
                entity.addViewer(this);
            }
            if (entity instanceof Player && isAutoViewable() && !viewers.contains(entity)) {
                addViewer((Player) entity);
            }
        });
    }

    /**
     * Gets the player connection.
     * <p>
     * Used to send packets and get stuff related to the connection.
     *
     * @return the player connection
     */
    @NotNull
    public PlayerConnection getPlayerConnection() {
        return playerConnection;
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
    @NotNull
    public PlayerSettings getSettings() {
        return settings;
    }

    /**
     * Gets the player dimension.
     *
     * @return the player current dimension
     */
    public DimensionType getDimensionType() {
        return dimensionType;
    }

    @NotNull
    public PlayerInventory getInventory() {
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
     * Changes the player {@link GameMode}.
     *
     * @param gameMode the new player GameMode
     */
    public void setGameMode(@NotNull GameMode gameMode) {
        this.gameMode = gameMode;
        // Condition to prevent sending the packets before spawning the player
        if (isActive()) {
            sendChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, gameMode.getId());

            PlayerInfoPacket infoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE);
            infoPacket.playerInfos.add(new PlayerInfoPacket.UpdateGamemode(getUuid(), gameMode));
            sendPacketToViewersAndSelf(infoPacket);
        }
    }

    /**
     * Gets if this player is in creative. Used for code readability.
     *
     * @return true if the player is in creative mode
     */
    public boolean isCreative() {
        return gameMode == GameMode.CREATIVE;
    }

    /**
     * Changes the dimension of the player.
     * Mostly unsafe since it requires sending chunks after.
     *
     * @param dimensionType the new player dimension
     */
    protected void sendDimension(@NotNull DimensionType dimensionType) {
        Check.argCondition(dimensionType.equals(getDimensionType()),
                "The dimension needs to be different than the current one!");
        this.dimensionType = dimensionType;
        RespawnPacket respawnPacket = new RespawnPacket();
        respawnPacket.dimensionType = dimensionType;
        respawnPacket.gameMode = gameMode;
        respawnPacket.isFlat = levelFlat;
        playerConnection.sendPacket(respawnPacket);
    }

    /**
     * Kicks the player with a reason.
     *
     * @param component the reason
     */
    public void kick(@NotNull Component component) {
        final ConnectionState connectionState = playerConnection.getConnectionState();
        // Packet type depends on the current player connection state
        final ServerPacket disconnectPacket;
        if (connectionState == ConnectionState.LOGIN) {
            disconnectPacket = new LoginDisconnectPacket(component);
        } else {
            disconnectPacket = new DisconnectPacket(component);
        }
        if (playerConnection instanceof PlayerSocketConnection) {
            ((PlayerSocketConnection) playerConnection).writeAndFlush(disconnectPacket);
            playerConnection.disconnect();
        } else {
            playerConnection.sendPacket(disconnectPacket);
            playerConnection.refreshOnline(false);
        }
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
        this.playerConnection.sendPacket(new HeldItemChangePacket(slot));
    }

    /**
     * Gets the player held slot (0-8).
     *
     * @return the current held slot for the player
     */
    public byte getHeldSlot() {
        return heldSlot;
    }

    public void setTeam(Team team) {
        super.setTeam(team);
        if (team != null) {
            var players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            PacketUtils.sendGroupedPacket(players, team.createTeamsCreationPacket());
        }
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

    /**
     * Gets the player open inventory.
     *
     * @return the currently open inventory, null if there is not (player inventory is not detected)
     */
    @Nullable
    public Inventory getOpenInventory() {
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
            Inventory openInventory = getOpenInventory();
            if (openInventory != null) {
                openInventory.removeViewer(this);
            }

            Inventory newInventory = inventoryOpenEvent.getInventory();

            if (newInventory == null) {
                // just close the inventory
                return;
            }

            OpenWindowPacket openWindowPacket = new OpenWindowPacket(newInventory.getTitle());
            openWindowPacket.windowId = newInventory.getWindowId();
            openWindowPacket.windowType = newInventory.getInventoryType().getWindowType();
            playerConnection.sendPacket(openWindowPacket);
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
        Inventory openInventory = getOpenInventory();

        // Drop cursor item when closing inventory
        ItemStack cursorItem;
        if (openInventory == null) {
            cursorItem = getInventory().getCursorItem();
            getInventory().setCursorItem(ItemStack.AIR);
        } else {
            cursorItem = openInventory.getCursorItem(this);
            openInventory.setCursorItem(this, ItemStack.AIR);
        }
        if (!cursorItem.isAir()) {
            // Add item to inventory if he hasn't been able to drop it
            if (!dropItem(cursorItem)) {
                getInventory().addItemStack(cursorItem);
            }
        }

        CloseWindowPacket closeWindowPacket = new CloseWindowPacket();
        if (openInventory == null) {
            closeWindowPacket.windowId = 0;
        } else {
            closeWindowPacket.windowId = openInventory.getWindowId();
            openInventory.removeViewer(this); // Clear cache
            this.openInventory = null;
        }
        playerConnection.sendPacket(closeWindowPacket);
        inventory.update();
        this.didCloseInventory = true;
    }

    /**
     * Used internally to prevent an inventory click to be processed
     * when the inventory listeners closed the inventory.
     * <p>
     * Should only be used within an inventory listener (event or condition).
     *
     * @return true if the inventory has been closed, false otherwise
     */
    public boolean didCloseInventory() {
        return didCloseInventory;
    }

    /**
     * Used internally to reset the didCloseInventory field.
     * <p>
     * Shouldn't be used externally without proper understanding of its consequence.
     *
     * @param didCloseInventory the new didCloseInventory field
     */
    public void UNSAFE_changeDidCloseInventory(boolean didCloseInventory) {
        this.didCloseInventory = didCloseInventory;
    }

    /**
     * Gets the player viewable chunks.
     * <p>
     * WARNING: adding or removing a chunk there will not load/unload it,
     * use {@link Chunk#addViewer(Player)} or {@link Chunk#removeViewer(Player)}.
     *
     * @return a {@link Set} containing all the chunks that the player sees
     */
    public Set<Chunk> getViewableChunks() {
        return viewableChunks;
    }

    /**
     * Sends a {@link UpdateViewPositionPacket}  to the player.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    public void updateViewPosition(int chunkX, int chunkZ) {
        playerConnection.sendPacket(new UpdateViewPositionPacket(chunkX, chunkZ));
    }

    public int getLastSentTeleportId() {
        return teleportId.get();
    }

    public int getLastReceivedTeleportId() {
        return receivedTeleportId;
    }

    public void refreshReceivedTeleportId(int receivedTeleportId) {
        this.receivedTeleportId = receivedTeleportId;
    }

    /**
     * @see Entity#synchronizePosition(boolean)
     */
    @Override
    @ApiStatus.Internal
    protected void synchronizePosition(boolean includeSelf) {
        if (includeSelf) {
            playerConnection.sendPacket(new PlayerPositionAndLookPacket(position, (byte) 0x00, teleportId.incrementAndGet(), false));
        }
        super.synchronizePosition(includeSelf);
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

        // Magic values: https://wiki.vg/Entity_statuses#Player
        // TODO remove magic values
        final byte permissionLevelStatus = (byte) (24 + permissionLevel);
        triggerStatus(permissionLevelStatus);
    }

    /**
     * Sets or remove the reduced debug screen.
     *
     * @param reduced should the player has the reduced debug screen
     */
    public void setReducedDebugScreenInformation(boolean reduced) {
        this.reducedDebugScreenInformation = reduced;

        // Magic values: https://wiki.vg/Entity_statuses#Player
        // TODO remove magic values
        final byte debugScreenStatus = (byte) (reduced ? 22 : 23);
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
     * The invulnerable field appear in the {@link PlayerAbilitiesPacket} packet.
     *
     * @return true if the player is invulnerable, false otherwise
     */
    public boolean isInvulnerable() {
        return super.isInvulnerable();
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
        this.flying = flying;
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
     * @see <a href="https://wiki.vg/Protocol#Player_Abilities_.28clientbound.29">player abilities</a>
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
     * Gets the player vehicle information.
     *
     * @return the player vehicle information
     */
    public @NotNull PlayerVehicleInformation getVehicleInformation() {
        return vehicleInformation;
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
        playerConnection.sendPacket(new PlayerAbilitiesPacket(flags, flyingSpeed, fieldViewModifier));
    }

    /**
     * All packets in the queue are executed in the {@link #update(long)} method
     * It is used internally to add all received packet from the client.
     * Could be used to "simulate" a received packet, but to use at your own risk.
     *
     * @param packet the packet to add in the queue
     */
    public void addPacketToQueue(@NotNull ClientPlayPacket packet) {
        this.packets.add(packet);
    }

    /**
     * Changes the storage player latency and update its tab value.
     *
     * @param latency the new player latency
     */
    public void refreshLatency(int latency) {
        this.latency = latency;
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(getUuid(), latency));
        sendPacketToViewersAndSelf(playerInfoPacket);
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
     * Also cancel eating if {@link #isEating()} was true.
     * <p>
     * Warning: the player will not be noticed by this chance, only his viewers,
     * see instead: {@link #setHeldItemSlot(byte)}.
     *
     * @param slot the new held slot
     */
    public void refreshHeldSlot(byte slot) {
        this.heldSlot = slot;
        syncEquipment(EquipmentSlot.MAIN_HAND);
        refreshEating(null);
    }

    public void refreshEating(@Nullable Hand eatingHand, long eatingTime) {
        this.eatingHand = eatingHand;
        if (eatingHand != null) {
            this.startEatingTime = System.currentTimeMillis();
            this.eatingTime = eatingTime;
        } else {
            this.startEatingTime = 0;
        }
    }

    public void refreshEating(@Nullable Hand eatingHand) {
        refreshEating(eatingHand, defaultEatingTime);
    }

    /**
     * Used to call {@link ItemUpdateStateEvent} with the proper item
     * It does check which hand to get the item to update.
     *
     * @param allowFood true if food should be updated, false otherwise
     * @return the called {@link ItemUpdateStateEvent},
     * null if there is no item to update the state
     * @deprecated Use {@link #callItemUpdateStateEvent(Hand)} instead
     */
    @Deprecated
    public @Nullable ItemUpdateStateEvent callItemUpdateStateEvent(boolean allowFood, @Nullable Hand hand) {
        if (hand == null)
            return null;

        final ItemStack updatedItem = getItemInHand(hand);
        final boolean isFood = updatedItem.getMaterial().isFood();

        if (isFood && !allowFood)
            return null;

        ItemUpdateStateEvent itemUpdateStateEvent = new ItemUpdateStateEvent(this, hand, updatedItem);
        EventDispatcher.call(itemUpdateStateEvent);

        return itemUpdateStateEvent;
    }

    /**
     * Used to call {@link ItemUpdateStateEvent} with the proper item
     * It does check which hand to get the item to update. Allows food.
     *
     * @return the called {@link ItemUpdateStateEvent},
     * null if there is no item to update the state
     */
    public @Nullable ItemUpdateStateEvent callItemUpdateStateEvent(@Nullable Hand hand) {
        return callItemUpdateStateEvent(true, hand);
    }

    public void refreshVehicleSteer(float sideways, float forward, boolean jump, boolean unmount) {
        this.vehicleInformation.refresh(sideways, forward, jump, unmount);
    }

    /**
     * @return the chunk range of the viewers,
     * which is {@link MinecraftServer#getChunkViewDistance()} or {@link PlayerSettings#getViewDistance()}
     * based on which one is the lowest
     */
    public int getChunkRange() {
        return Math.min(getSettings().viewDistance, MinecraftServer.getChunkViewDistance());
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
        return HoverEvent.showEntity(ShowEntity.of(EntityType.PLAYER, this.uuid, this.displayName));
    }

    /**
     * Gets the packet to add the player from the tab-list.
     *
     * @return a {@link PlayerInfoPacket} to add the player
     */
    @NotNull
    protected PlayerInfoPacket getAddPlayerToList() {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);

        PlayerInfoPacket.AddPlayer addPlayer =
                new PlayerInfoPacket.AddPlayer(getUuid(), getUsername(), getGameMode(), getLatency());
        addPlayer.displayName = displayName;

        // Skin support
        if (skin != null) {
            final String textures = skin.getTextures();
            final String signature = skin.getSignature();

            PlayerInfoPacket.AddPlayer.Property prop =
                    new PlayerInfoPacket.AddPlayer.Property("textures", textures, signature);
            addPlayer.properties.add(prop);
        }

        playerInfoPacket.playerInfos.add(addPlayer);
        return playerInfoPacket;
    }

    /**
     * Gets the packet to remove the player from the tab-list.
     *
     * @return a {@link PlayerInfoPacket} to remove the player
     */
    @NotNull
    protected PlayerInfoPacket getRemovePlayerToList() {
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);

        PlayerInfoPacket.RemovePlayer removePlayer =
                new PlayerInfoPacket.RemovePlayer(getUuid());

        playerInfoPacket.playerInfos.add(removePlayer);
        return playerInfoPacket;
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
        connection.sendPacket(getAddPlayerToList());
        connection.sendPacket(getEntityType().registry().spawnType().getSpawnPacket(this));
        connection.sendPacket(getVelocityPacket());
        connection.sendPacket(getMetadataPacket());
        connection.sendPacket(getEquipmentsPacket());
        if (hasPassenger()) {
            connection.sendPacket(getPassengersPacket());
        }
        // Team
        if (this.getTeam() != null) {
            connection.sendPacket(this.getTeam().createTeamsCreationPacket());
        }
        connection.sendPacket(new EntityHeadLookPacket(getEntityId(), position.yaw()));
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return inventory.getItemInMainHand();
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        inventory.setItemInMainHand(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return inventory.getItemInOffHand();
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        inventory.setItemInOffHand(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getHelmet() {
        return inventory.getHelmet();
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        inventory.setHelmet(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getChestplate() {
        return inventory.getChestplate();
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        inventory.setChestplate(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getLeggings() {
        return inventory.getLeggings();
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        inventory.setLeggings(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getBoots() {
        return inventory.getBoots();
    }

    @Override
    public void setBoots(@NotNull ItemStack itemStack) {
        inventory.setBoots(itemStack);
    }

    @Override
    public Locale getLocale() {
        return settings.locale == null ? null : Locale.forLanguageTag(settings.locale);
    }

    /**
     * Sets the player's locale. This will only set the locale of the player as it
     * is stored in the server. This will also be reset if the settings are refreshed.
     *
     * @param locale the new locale
     */
    @Override
    public void setLocale(@Nullable Locale locale) {
        settings.locale = locale == null ? null : locale.toLanguageTag();
    }

    @Override
    public @NotNull Identity identity() {
        return this.identity;
    }

    @Override
    public @NotNull Pointers pointers() {
        return this.pointers;
    }

    @Override
    public void setUuid(@NotNull UUID uuid) {
        super.setUuid(uuid);
        // update identity
        this.identity = Identity.identity(uuid);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public Player asPlayer() {
        return this;
    }

    /**
     * Represents the main or off hand of the player.
     */
    public enum Hand {
        MAIN,
        OFF
    }

    public enum FacePoint {
        FEET,
        EYE
    }

    // Settings enum

    /**
     * Represents where is located the main hand of the player (can be changed in Minecraft option).
     */
    public enum MainHand {
        LEFT,
        RIGHT
    }

    /**
     * @deprecated See {@link ChatMessageType}
     */
    @Deprecated
    public enum ChatMode {
        ENABLED,
        COMMANDS_ONLY,
        HIDDEN
    }

    public class PlayerSettings {

        private String locale;
        private byte viewDistance;
        private ChatMessageType chatMessageType;
        private boolean chatColors;
        private byte displayedSkinParts;
        private MainHand mainHand;

        public PlayerSettings() {
            viewDistance = 2;
        }

        /**
         * The player game language.
         *
         * @return the player locale
         */
        public String getLocale() {
            return locale;
        }

        /**
         * Gets the player view distance.
         *
         * @return the player view distance
         */
        public byte getViewDistance() {
            return viewDistance;
        }

        /**
         * Gets the player chat mode.
         *
         * @return the player chat mode
         * @deprecated Use {@link #getChatMessageType()}
         */
        @Deprecated
        public ChatMode getChatMode() {
            return ChatMode.values()[chatMessageType.ordinal()];
        }

        /**
         * Gets the messages this player wants to receive.
         *
         * @return the messages
         */
        public @Nullable ChatMessageType getChatMessageType() {
            return chatMessageType;
        }

        /**
         * Gets if the player has chat colors enabled.
         *
         * @return true if chat colors are enabled, false otherwise
         */
        public boolean hasChatColors() {
            return chatColors;
        }

        public byte getDisplayedSkinParts() {
            return displayedSkinParts;
        }

        /**
         * Gets the player main hand.
         *
         * @return the player main hand
         */
        public MainHand getMainHand() {
            return mainHand;
        }

        /**
         * Changes the player settings internally.
         * <p>
         * WARNING: the player will not be noticed by this change, probably unsafe.
         *
         * @param locale             the player locale
         * @param viewDistance       the player view distance
         * @param chatMessageType    the chat messages the player wishes to receive
         * @param chatColors         if chat colors should be displayed
         * @param displayedSkinParts the player displayed skin parts
         * @param mainHand           the player main hand
         */
        public void refresh(String locale, byte viewDistance, ChatMessageType chatMessageType, boolean chatColors,
                            byte displayedSkinParts, MainHand mainHand) {

            final boolean viewDistanceChanged = this.viewDistance != viewDistance;

            this.locale = locale;
            this.viewDistance = viewDistance;
            this.chatMessageType = chatMessageType;
            this.chatColors = chatColors;
            this.displayedSkinParts = displayedSkinParts;
            this.mainHand = mainHand;

            // TODO: Use the metadata object here
            metadata.setIndex((byte) 17, Metadata.Byte(displayedSkinParts));

            // Client changed his view distance in the settings
            if (viewDistanceChanged) {
                refreshVisibleChunks();
            }
        }

    }

}
