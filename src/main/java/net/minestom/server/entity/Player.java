package net.minestom.server.entity;

import com.google.common.collect.Queues;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
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
import net.minestom.server.chat.ChatParser;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.vehicle.PlayerVehicleInformation;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.item.PickupExperienceEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.WrittenBookMeta;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.resourcepack.ResourcePack;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.stat.PlayerStatistic;
import net.minestom.server.utils.*;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.entity.EntityUtils;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.minestom.server.utils.instance.InstanceUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

/**
 * Those are the major actors of the server,
 * they are not necessary backed by a {@link NettyPlayerConnection} as shown by {@link FakePlayer}.
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

    private final Queue<ClientPlayPacket> packets = Queues.newConcurrentLinkedQueue();
    private final boolean levelFlat;
    private final PlayerSettings settings;
    private float exp;
    private int level;

    protected PlayerInventory inventory;
    private Inventory openInventory;
    // Used internally to allow the closing of inventory within the inventory listener
    private boolean didCloseInventory;

    private byte heldSlot;

    private Position respawnPoint;

    private int food;
    private float foodSaturation;
    private long startEatingTime;
    private long defaultEatingTime = 1000L;
    private long eatingTime;
    private Hand eatingHand;

    // Game state (https://wiki.vg/Protocol#Change_Game_State)
    private boolean enableRespawnScreen;

    // CustomBlock break delay
    private CustomBlock targetCustomBlock;
    private BlockPosition targetBlockPosition;
    // The last break delay requested
    private long targetBreakDelay;
    // Number of tick since the last stage change
    private long targetBlockBreakCount;
    // The current stage of the target block, only if multi player breaking is disabled
    private byte targetStage;
    // Only used if multi player breaking is disabled, contains only this player
    private final Set<Player> targetBreakers = Collections.singleton(this);

    // Experience orb pickup
    protected Cooldown experiencePickupCooldown = new Cooldown(new UpdateOption(10, TimeUnit.TICK));

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

    public Player(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(EntityType.PLAYER, uuid);
        this.username = username;
        this.usernameComponent = Component.text(username);
        this.playerConnection = playerConnection;

        setBoundingBox(0.6f, 1.8f, 0.6f);

        setRespawnPoint(new Position(0, 0, 0));

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
    }

    /**
     * Used when the player is created.
     * Init the player and spawn him.
     * <p>
     * WARNING: executed in the main update thread
     * UNSAFE: Only meant to be used when a netty player connects through the server.
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
        {
            playerConnection.sendPacket(PluginMessagePacket.getBrandPacket());
        }

        ServerDifficultyPacket serverDifficultyPacket = new ServerDifficultyPacket();
        serverDifficultyPacket.difficulty = MinecraftServer.getDifficulty();
        serverDifficultyPacket.locked = true;
        playerConnection.sendPacket(serverDifficultyPacket);

        SpawnPositionPacket spawnPositionPacket = new SpawnPositionPacket();
        spawnPositionPacket.x = (int) respawnPoint.getX();
        spawnPositionPacket.y = (int) respawnPoint.getY();
        spawnPositionPacket.z = (int) respawnPoint.getZ();
        playerConnection.sendPacket(spawnPositionPacket);

        // Add player to list with spawning skin
        PlayerSkinInitEvent skinInitEvent = new PlayerSkinInitEvent(this, skin);
        callEvent(PlayerSkinInitEvent.class, skinInitEvent);
        this.skin = skinInitEvent.getSkin();
        // FIXME: when using Geyser, this line remove the skin of the client
        playerConnection.sendPacket(getAddPlayerToList());

        // Commands start
        {
            CommandManager commandManager = MinecraftServer.getCommandManager();
            DeclareCommandsPacket declareCommandsPacket = commandManager.createDeclareCommandsPacket(this);

            playerConnection.sendPacket(declareCommandsPacket);
        }
        // Commands end


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

        // Tags start
        {
            TagsPacket tags = TagsPacket.getRequiredTagsPacket();

            UpdateTagListEvent event = new UpdateTagListEvent(tags);
            callEvent(UpdateTagListEvent.class, event);

            this.playerConnection.sendPacket(tags);
        }
        // Tags end

        // Some client update
        this.playerConnection.sendPacket(getPropertiesPacket()); // Send default properties
        refreshHealth(); // Heal and send health packet
        refreshAbilities(); // Send abilities packet
        getInventory().update();
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

        // Target block stage
        if (targetCustomBlock != null) {
            this.targetBlockBreakCount++;

            final boolean processStage = targetBreakDelay < 0 || targetBlockBreakCount >= targetBreakDelay;

            // Check if the player did finish his current break delay
            if (processStage) {

                // Negative value should skip abs(value) stage
                final byte stageIncrease = (byte) (targetBreakDelay > 0 ? 1 : Math.abs(targetBreakDelay));

                // Should increment the target block stage
                if (targetCustomBlock.enableMultiPlayerBreaking()) {
                    // Let the custom block object manages the breaking
                    final boolean canContinue = targetCustomBlock.processStage(instance, targetBlockPosition, this, stageIncrease);
                    if (canContinue) {
                        final Set<Player> breakers = targetCustomBlock.getBreakers(instance, targetBlockPosition);
                        refreshBreakDelay(breakers);
                    } else {
                        resetTargetBlock();
                    }
                } else {
                    // Let the player object manages the breaking
                    // The custom block doesn't support multi player breaking
                    if (targetStage + stageIncrease >= CustomBlock.MAX_STAGE) {
                        // Break the block
                        instance.breakBlock(this, targetBlockPosition);
                        resetTargetBlock();
                    } else {
                        // Send the new block break animation packet and refresh data

                        final Chunk chunk = instance.getChunkAt(targetBlockPosition);
                        final int entityId = targetCustomBlock.getBreakEntityId(this);
                        final BlockBreakAnimationPacket blockBreakAnimationPacket =
                                new BlockBreakAnimationPacket(entityId, targetBlockPosition, targetStage);
                        Check.notNull(chunk, "Tried to interact with an unloaded chunk.");
                        chunk.sendPacketToViewers(blockBreakAnimationPacket);

                        refreshBreakDelay(targetBreakers);
                        this.targetStage += stageIncrease;
                    }
                }
            }
        }

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
                        callCancellableEvent(PickupExperienceEvent.class, pickupExperienceEvent, () -> {
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
                ItemUpdateStateEvent itemUpdateStateEvent = callItemUpdateStateEvent(true, eatingHand);

                Check.notNull(itemUpdateStateEvent, "#callItemUpdateStateEvent returned null.");

                // Refresh hand
                final boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
                refreshActiveHand(false, isOffHand, false);

                final ItemStack foodItem = itemUpdateStateEvent.getItemStack();
                final boolean isFood = foodItem.getMaterial().isFood();

                if (isFood) {
                    PlayerEatEvent playerEatEvent = new PlayerEatEvent(this, foodItem, eatingHand);
                    callEvent(PlayerEatEvent.class, playerEatEvent);
                }

                refreshEating(null);
            }
        }

        // Tick event
        callEvent(PlayerTickEvent.class, playerTickEvent);
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
            callEvent(PlayerDeathEvent.class, playerDeathEvent);

            deathText = playerDeathEvent.getDeathText();
            chatMessage = playerDeathEvent.getChatMessage();

            // #buildDeathScreenText can return null, check here
            if (deathText != null) {
                CombatEventPacket deathPacket = CombatEventPacket.death(this, null, deathText);
                playerConnection.sendPacket(deathPacket);
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
        callEvent(PlayerRespawnEvent.class, respawnEvent);
        refreshIsDead(false);

        // Runnable called when teleportation is successful (after loading and sending necessary chunk)
        teleport(respawnEvent.getRespawnPosition(), this::refreshAfterTeleport);
    }

    @Override
    public void spawn() {

    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void remove() {
        if (isRemoved())
            return;

        callEvent(PlayerDisconnectEvent.class, new PlayerDisconnectEvent(this));

        super.remove();
        this.packets.clear();
        if (getOpenInventory() != null)
            getOpenInventory().removeViewer(this);

        // Boss bars cache
        {
            Set<net.minestom.server.bossbar.BossBar> bossBars = net.minestom.server.bossbar.BossBar.getBossBars(this);
            if (bossBars != null) {
                for (net.minestom.server.bossbar.BossBar bossBar : bossBars) {
                    bossBar.removeViewer(this);
                }
            }
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
        this.viewableChunks.forEach(chunk -> {
            if (chunk.isLoaded())
                chunk.removeViewer(this);
        });
        resetTargetBlock();
        playerConnection.disconnect();
    }

    @Override
    protected boolean addViewer0(@NotNull Player player) {
        if (player == this) {
            return false;
        }
        PlayerConnection viewerConnection = player.getPlayerConnection();
        viewerConnection.sendPacket(getAddPlayerToList());
        return super.addViewer0(player);
    }

    @Override
    protected boolean removeViewer0(@NotNull Player player) {
        if (player == this || !super.removeViewer0(player)) {
            return false;
        }

        PlayerConnection viewerConnection = player.getPlayerConnection();
        viewerConnection.sendPacket(getRemovePlayerToList());

        // Team
        if (this.getTeam() != null && this.getTeam().getMembers().size() == 1) {// If team only contains "this" player
            viewerConnection.sendPacket(this.getTeam().createTeamDestructionPacket());
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
     */
    @Override
    public void setInstance(@NotNull Instance instance, @NotNull Position spawnPosition) {
        Check.argCondition(this.instance == instance, "Instance should be different than the current one");

        // true if the chunks need to be sent to the client, can be false if the instances share the same chunks (eg SharedInstance)
        final boolean needWorldRefresh = !InstanceUtils.areLinked(this.instance, instance) ||
                !spawnPosition.inSameChunk(this.position);

        if (needWorldRefresh) {
            // TODO: Handle player reconnections, must be false in that case too
            final boolean firstSpawn = this.instance == null;

            // Send the new dimension if player isn't in any instance or if the dimension is different
            final DimensionType instanceDimensionType = instance.getDimensionType();
            final boolean dimensionChange = dimensionType != instanceDimensionType;
            if (dimensionChange) {
                sendDimension(instanceDimensionType);
            }

            // Only load the spawning chunk to speed up login, remaining chunks are loaded in #spawnPlayer
            final long[] visibleChunks = ChunkUtils.getChunksInRange(spawnPosition, 0);

            final ChunkCallback endCallback =
                    chunk -> spawnPlayer(instance, spawnPosition, firstSpawn, dimensionChange, true);

            ChunkUtils.optionalLoadAll(instance, visibleChunks, null, endCallback);
        } else {
            // The player already has the good version of all the chunks.
            // We just need to refresh his entity viewing list and add him to the instance
            spawnPlayer(instance, spawnPosition, false, false, false);
        }
    }

    /**
     * Changes the player instance without changing its position (defaulted to {@link #getRespawnPoint()}
     * if the player is not in any instance).
     *
     * @param instance the new player instance
     * @see #setInstance(Instance, Position)
     */
    @Override
    public void setInstance(@NotNull Instance instance) {
        setInstance(instance, this.instance != null ? getPosition() : getRespawnPoint());
    }

    /**
     * Used to spawn the player once the client has all the required chunks.
     * <p>
     * Does add the player to {@code instance}, remove all viewable entities and call {@link PlayerSpawnEvent}.
     * <p>
     * UNSAFE: only called with {@link #setInstance(Instance, Position)}.
     *
     * @param spawnPosition the position to teleport the player
     * @param firstSpawn    true if this is the player first spawn
     * @param updateChunks  true if chunks should be refreshed, false if the new instance shares the same
     *                      chunks
     */
    private void spawnPlayer(@NotNull Instance instance, @NotNull Position spawnPosition,
                             boolean firstSpawn, boolean dimensionChange, boolean updateChunks) {
        if (!firstSpawn) {
            // Player instance changed, clear current viewable collections
            this.viewableChunks.forEach(chunk -> chunk.removeViewer(this));
            this.viewableEntities.forEach(entity -> entity.removeViewer(this));
        }

        super.setInstance(instance, spawnPosition);

        if (updateChunks) {
            refreshVisibleChunks();
        }

        if (dimensionChange || firstSpawn) {
            synchronizePosition(); // So the player doesn't get stuck
            this.inventory.update();
        }

        PlayerSpawnEvent spawnEvent = new PlayerSpawnEvent(this, instance, firstSpawn);
        callEvent(PlayerSpawnEvent.class, spawnEvent);
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel the message channel
     * @param data    the message data
     */
    public void sendPluginMessage(@NotNull String channel, @NotNull byte[] data) {
        PluginMessagePacket pluginMessagePacket = new PluginMessagePacket();
        pluginMessagePacket.channel = channel;
        pluginMessagePacket.data = data;
        playerConnection.sendPacket(pluginMessagePacket);
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
        final byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        sendPluginMessage(channel, bytes);
    }

    /**
     * Sends a legacy message with the specified color char.
     *
     * @param text      the text with the legacy color formatting
     * @param colorChar the color character
     * @deprecated Use {@link #sendMessage(Component)}
     */
    @Deprecated
    public void sendLegacyMessage(@NotNull String text, char colorChar) {
        ColoredText coloredText = ColoredText.ofLegacy(text, colorChar);
        sendJsonMessage(coloredText.toString());
    }

    /**
     * Sends a legacy message with the default color char {@link ChatParser#COLOR_CHAR}.
     *
     * @param text the text with the legacy color formatting
     * @deprecated Use {@link #sendMessage(Component)}
     */
    @Deprecated
    public void sendLegacyMessage(@NotNull String text) {
        ColoredText coloredText = ColoredText.ofLegacy(text, ChatParser.COLOR_CHAR);
        sendJsonMessage(coloredText.toString());
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
        ChatMessagePacket chatMessagePacket = new ChatMessagePacket(message, ChatMessagePacket.Position.fromMessageType(type), source.uuid());
        playerConnection.sendPacket(chatMessagePacket);
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

    /**
     * Plays a sound from the {@link SoundEvent} enum.
     *
     * @param sound         the sound to play
     * @param soundCategory the sound category
     * @param x             the effect X
     * @param y             the effect Y
     * @param z             the effect Z
     * @param volume        the volume of the sound (1 is 100%)
     * @param pitch         the pitch of the sound, between 0.5 and 2.0
     * @deprecated Use {@link #playSound(net.kyori.adventure.sound.Sound, double, double, double)}
     */
    @Deprecated
    public void playSound(@NotNull SoundEvent sound, @NotNull SoundCategory soundCategory, int x, int y, int z, float volume, float pitch) {
        SoundEffectPacket soundEffectPacket = new SoundEffectPacket();
        soundEffectPacket.soundId = sound.getId();
        soundEffectPacket.soundSource = soundCategory.asSource();
        soundEffectPacket.x = x;
        soundEffectPacket.y = y;
        soundEffectPacket.z = z;
        soundEffectPacket.volume = volume;
        soundEffectPacket.pitch = pitch;
        playerConnection.sendPacket(soundEffectPacket);
    }

    /**
     * Plays a sound from the {@link SoundEvent} enum.
     *
     * @see #playSound(SoundEvent, SoundCategory, int, int, int, float, float)
     * @deprecated Use {@link #playSound(net.kyori.adventure.sound.Sound, double, double, double)}
     */
    @Deprecated
    public void playSound(@NotNull SoundEvent sound, @NotNull SoundCategory soundCategory, BlockPosition position, float volume, float pitch) {
        playSound(sound, soundCategory, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    /**
     * Plays a sound from an identifier (represents a custom sound in a resource pack).
     *
     * @param identifier    the identifier of the sound to play
     * @param soundCategory the sound category
     * @param x             the effect X
     * @param y             the effect Y
     * @param z             the effect Z
     * @param volume        the volume of the sound (1 is 100%)
     * @param pitch         the pitch of the sound, between 0.5 and 2.0
     * @deprecated Use {@link #playSound(net.kyori.adventure.sound.Sound, double, double, double)}
     */
    @Deprecated
    public void playSound(@NotNull String identifier, @NotNull SoundCategory soundCategory, int x, int y, int z, float volume, float pitch) {
        NamedSoundEffectPacket namedSoundEffectPacket = new NamedSoundEffectPacket();
        namedSoundEffectPacket.soundName = identifier;
        namedSoundEffectPacket.soundSource = soundCategory.asSource();
        namedSoundEffectPacket.x = x;
        namedSoundEffectPacket.y = y;
        namedSoundEffectPacket.z = z;
        namedSoundEffectPacket.volume = volume;
        namedSoundEffectPacket.pitch = pitch;
        playerConnection.sendPacket(namedSoundEffectPacket);
    }

    /**
     * Plays a sound from an identifier (represents a custom sound in a resource pack).
     *
     * @see #playSound(String, SoundCategory, int, int, int, float, float)
     * @deprecated Use {@link #playSound(net.kyori.adventure.sound.Sound, double, double, double)}
     */
    @Deprecated
    public void playSound(@NotNull String identifier, @NotNull SoundCategory soundCategory, BlockPosition position, float volume, float pitch) {
        playSound(identifier, soundCategory, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    /**
     * Plays a sound directly to the player (constant volume).
     *
     * @param sound         the sound to play
     * @param soundCategory the sound category
     * @param volume        the volume of the sound (1 is 100%)
     * @param pitch         the pitch of the sound, between 0.5 and 2.0
     * @deprecated Use {@link #playSound(net.kyori.adventure.sound.Sound)}
     */
    @Deprecated
    public void playSound(@NotNull SoundEvent sound, @NotNull SoundCategory soundCategory, float volume, float pitch) {
        EntitySoundEffectPacket entitySoundEffectPacket = new EntitySoundEffectPacket();
        entitySoundEffectPacket.entityId = getEntityId();
        entitySoundEffectPacket.soundId = sound.getId();
        entitySoundEffectPacket.soundSource = soundCategory.asSource();
        entitySoundEffectPacket.volume = volume;
        entitySoundEffectPacket.pitch = pitch;
        playerConnection.sendPacket(entitySoundEffectPacket);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        playerConnection.sendPacket(AdventurePacketConvertor.createEntitySoundPacket(sound, this));
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        playerConnection.sendPacket(AdventurePacketConvertor.createSoundPacket(sound, x, y, z));
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
        packet.position = new BlockPosition(x, y, z);
        packet.data = data;
        packet.disableRelativeVolume = disableRelativeVolume;
        playerConnection.sendPacket(packet);
    }

    /**
     * Sends a {@link StopSoundPacket} packet.
     *
     * @deprecated Use {@link #stopSound(SoundStop)} with {@link SoundStop#all()}
     */
    @Deprecated
    public void stopSound() {
        StopSoundPacket stopSoundPacket = new StopSoundPacket();
        stopSoundPacket.flags = 0x00;
        playerConnection.sendPacket(stopSoundPacket);
    }

    /**
     * Sets the header and footer of a player which will be displayed in his tab window.
     *
     * @param header the header text, null to set empty
     * @param footer the footer text, null to set empty
     * @deprecated Use {@link #sendPlayerListHeaderAndFooter(Component, Component)}
     */
    @Deprecated
    public void sendHeaderFooter(@Nullable JsonMessage header, @Nullable JsonMessage footer) {
        this.sendPlayerListHeaderAndFooter(header == null ? Component.empty() : header.asComponent(),
                footer == null ? Component.empty() : footer.asComponent());
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        PlayerListHeaderAndFooterPacket packet = new PlayerListHeaderAndFooterPacket(header, footer);
        playerConnection.sendPacket(packet);
    }

    /**
     * Common method to send a title.
     *
     * @param text   the text of the title
     * @param action the action of the title (where to show it)
     * @see #sendTitleTime(int, int, int) to specify the display time
     * @deprecated Use {@link #showTitle(Title)} and {@link #sendActionBar(Component)}
     */
    @Deprecated
    private void sendTitle(@NotNull JsonMessage text, @NotNull TitlePacket.Action action) {
        TitlePacket titlePacket = new TitlePacket(action, text.asComponent());
        playerConnection.sendPacket(titlePacket);
    }

    /**
     * Sends a title and subtitle message.
     *
     * @param title    the title message
     * @param subtitle the subtitle message
     * @see #sendTitleTime(int, int, int) to specify the display time
     * @deprecated Use {@link #showTitle(Title)}
     */
    @Deprecated
    public void sendTitleSubtitleMessage(@NotNull JsonMessage title, @NotNull JsonMessage subtitle) {
        this.showTitle(Title.title(title.asComponent(), subtitle.asComponent()));
    }

    /**
     * Sends a title message.
     *
     * @param title the title message
     * @see #sendTitleTime(int, int, int) to specify the display time
     * @deprecated Use {@link #showTitle(Title)}
     */
    @Deprecated
    public void sendTitleMessage(@NotNull JsonMessage title) {
        this.showTitle(Title.title(title.asComponent(), Component.empty()));
    }

    /**
     * Sends a subtitle message.
     *
     * @param subtitle the subtitle message
     * @see #sendTitleTime(int, int, int) to specify the display time
     * @deprecated Use {@link #showTitle(Title)}
     */
    @Deprecated
    public void sendSubtitleMessage(@NotNull JsonMessage subtitle) {
        this.showTitle(Title.title(Component.empty(), subtitle.asComponent()));
    }

    /**
     * Sends an action bar message.
     *
     * @param actionBar the action bar message
     * @see #sendTitleTime(int, int, int) to specify the display time
     * @deprecated Use {@link #sendActionBar(Component)}
     */
    @Deprecated
    public void sendActionBarMessage(@NotNull JsonMessage actionBar) {
        this.sendActionBar(actionBar.asComponent());
    }

    @Override
    public void showTitle(@NotNull Title title) {
        Collection<TitlePacket> packet = TitlePacket.of(Title.title(title.title(), title.subtitle(), title.times()));

        for (TitlePacket titlePacket : packet) {
            playerConnection.sendPacket(titlePacket);
        }
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        TitlePacket titlePacket = new TitlePacket(TitlePacket.Action.SET_ACTION_BAR, message);
        playerConnection.sendPacket(titlePacket);
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
        TitlePacket titlePacket = new TitlePacket(fadeIn, stay, fadeOut);
        playerConnection.sendPacket(titlePacket);
    }

    /**
     * Hides the previous title.
     *
     * @deprecated Use {@link #clearTitle()}
     */
    @Deprecated
    public void hideTitle() {
        TitlePacket titlePacket = new TitlePacket(TitlePacket.Action.HIDE);
        playerConnection.sendPacket(titlePacket);
    }

    @Override
    public void resetTitle() {
        TitlePacket titlePacket = new TitlePacket(TitlePacket.Action.RESET);
        playerConnection.sendPacket(titlePacket);
    }

    @Override
    public void clearTitle() {
        TitlePacket titlePacket = new TitlePacket(TitlePacket.Action.HIDE);
        playerConnection.sendPacket(titlePacket);
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
        ItemStack writtenBook = ItemStack.builder(Material.WRITTEN_BOOK)
                .meta(WrittenBookMeta.fromAdventure(book, this))
                .build();

        // Set book in offhand
        SetSlotPacket setBookPacket = new SetSlotPacket();
        setBookPacket.windowId = 0;
        setBookPacket.slot = PlayerInventoryUtils.OFFHAND_SLOT;
        setBookPacket.itemStack = writtenBook;
        playerConnection.sendPacket(setBookPacket);

        // Open the book
        OpenBookPacket openBookPacket = new OpenBookPacket();
        openBookPacket.hand = Hand.OFF;
        playerConnection.sendPacket(openBookPacket);

        // Restore the item in offhand
        SetSlotPacket restoreItemPacket = new SetSlotPacket();
        restoreItemPacket.windowId = 0;
        restoreItemPacket.slot = PlayerInventoryUtils.OFFHAND_SLOT;
        restoreItemPacket.itemStack = getItemInOffHand();
        playerConnection.sendPacket(restoreItemPacket);
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
        sendUpdateHealthPacket();
    }

    /**
     * Gets the player additional hearts.
     *
     * @return the player additional hearts
     */
    public float getAdditionalHearts() {
        return metadata.getIndex((byte) 14, 0f);
    }

    /**
     * Changes the amount of additional hearts shown.
     *
     * @param additionalHearts the count of additional hearts
     */
    public void setAdditionalHearts(float additionalHearts) {
        this.metadata.setIndex((byte) 14, Metadata.Float(additionalHearts));
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
        sendUpdateHealthPacket();
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
        sendUpdateHealthPacket();
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
     * @deprecated Use {@link #getDisplayName()}
     */
    @Nullable
    @Deprecated
    public JsonMessage getDisplayNameJson() {
        return JsonMessage.fromComponent(displayName);
    }

    /**
     * Gets the player display name in the tab-list.
     *
     * @return the player display name, null means that {@link #getUsername()} is displayed
     */
    @Nullable
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * Changes the player display name in the tab-list.
     * <p>
     * Sets to null to show the player username.
     *
     * @param displayName the display name, null to display the username
     * @deprecated Use {@link #setDisplayName(Component)}
     */
    @Deprecated
    public void setDisplayName(@Nullable JsonMessage displayName) {
        this.setDisplayName(displayName == null ? null : displayName.asComponent());
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
    @Nullable
    public PlayerSkin getSkin() {
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

        DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket();
        destroyEntitiesPacket.entityIds = new int[]{getEntityId()};

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
        callEvent(ItemDropEvent.class, itemDropEvent);
        return !itemDropEvent.isCancelled();
    }

    /**
     * Sets the player resource pack.
     *
     * @param resourcePack the resource pack
     */
    public void setResourcePack(@NotNull ResourcePack resourcePack) {
        final String url = resourcePack.getUrl();
        final String hash = resourcePack.getHash();

        ResourcePackSendPacket resourcePackSendPacket = new ResourcePackSendPacket();
        resourcePackSendPacket.url = url;
        resourcePackSendPacket.hash = hash;
        playerConnection.sendPacket(resourcePackSendPacket);
    }

    /**
     * Rotates the player to face {@code targetPosition}.
     *
     * @param facePoint      the point from where the player should aim
     * @param targetPosition the target position to face
     */
    public void facePosition(@NotNull FacePoint facePoint, @NotNull Position targetPosition) {
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

    private void facePosition(@NotNull FacePoint facePoint, @NotNull Position targetPosition,
                              @Nullable Entity entity, @Nullable FacePoint targetPoint) {
        FacePlayerPacket facePlayerPacket = new FacePlayerPacket();
        facePlayerPacket.entityFacePosition = facePoint == FacePoint.EYE ?
                FacePlayerPacket.FacePosition.EYES : FacePlayerPacket.FacePosition.FEET;
        facePlayerPacket.targetX = targetPosition.getX();
        facePlayerPacket.targetY = targetPosition.getY();
        facePlayerPacket.targetZ = targetPosition.getZ();
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
        CameraPacket cameraPacket = new CameraPacket();
        cameraPacket.cameraId = entity.getEntityId();
        playerConnection.sendPacket(cameraPacket);
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
     * Can be altered by the {@link PlayerRespawnEvent#setRespawnPosition(Position)}.
     *
     * @return a copy of the default respawn point
     */
    @NotNull
    public Position getRespawnPoint() {
        return respawnPoint.clone();
    }

    /**
     * Changes the default spawn point.
     *
     * @param respawnPoint the player respawn point
     */
    public void setRespawnPoint(@NotNull Position respawnPoint) {
        this.respawnPoint = respawnPoint;
    }

    /**
     * Called after the player teleportation to refresh his position
     * and send data to his new viewers.
     */
    protected void refreshAfterTeleport() {

        sendPacketsToViewers(getEntityType().getSpawnType().getSpawnPacket(this));

        // Update for viewers
        sendPacketToViewersAndSelf(getVelocityPacket());
        sendPacketToViewersAndSelf(getMetadataPacket());
        sendPacketToViewersAndSelf(getPropertiesPacket());
        sendPacketToViewersAndSelf(getEquipmentsPacket());

        getInventory().update();

        {
            // Send new chunks
            final BlockPosition pos = position.toBlockPosition();
            final Chunk chunk = instance.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
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
     * Sends an {@link UpdateHealthPacket} to refresh client-side information about health and food.
     */
    protected void sendUpdateHealthPacket() {
        UpdateHealthPacket updateHealthPacket = new UpdateHealthPacket();
        updateHealthPacket.health = getHealth();
        updateHealthPacket.food = food;
        updateHealthPacket.foodSaturation = foodSaturation;
        playerConnection.sendPacket(updateHealthPacket);
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
        sendExperienceUpdatePacket();
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
        sendExperienceUpdatePacket();
    }

    /**
     * Sends a {@link SetExperiencePacket} to refresh client-side information about the experience bar.
     */
    protected void sendExperienceUpdatePacket() {
        SetExperiencePacket setExperiencePacket = new SetExperiencePacket();
        setExperiencePacket.percentage = exp;
        setExperiencePacket.level = level;
        playerConnection.sendPacket(setExperiencePacket);
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
        final long[] lastVisibleChunks = viewableChunks.stream().mapToLong(viewableChunks ->
                ChunkUtils.getChunkIndex(viewableChunks.getChunkX(), viewableChunks.getChunkZ())
        ).toArray();

        // New chunks indexes
        final long[] updatedVisibleChunks = ChunkUtils.getChunksInRange(newChunk.toPosition(), getChunkRange());

        // Find the difference between the two arrays
        final int[] oldChunks = ArrayUtils.getDifferencesBetweenArray(lastVisibleChunks, updatedVisibleChunks);
        final int[] newChunks = ArrayUtils.getDifferencesBetweenArray(updatedVisibleChunks, lastVisibleChunks);

        // Update client render distance
        updateViewPosition(newChunk.getChunkX(), newChunk.getChunkZ());

        // Unload old chunks
        for (int index : oldChunks) {
            final long chunkIndex = lastVisibleChunks[index];
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);

            final UnloadChunkPacket unloadChunkPacket = new UnloadChunkPacket();
            unloadChunkPacket.chunkX = chunkX;
            unloadChunkPacket.chunkZ = chunkZ;
            playerConnection.sendPacket(unloadChunkPacket);

            final Chunk chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk != null)
                chunk.removeViewer(this);
        }

        // Load new chunks
        for (int index : newChunks) {
            final long chunkIndex = updatedVisibleChunks[index];
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);

            this.instance.loadOptionalChunk(chunkX, chunkZ, chunk -> {
                if (chunk == null) {
                    // Cannot load chunk (auto load is not enabled)
                    return;
                }
                chunk.addViewer(this);
            });
        }
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
     * @param text the kick reason
     * @deprecated Use {@link #kick(Component)}
     */
    @Deprecated
    public void kick(@NotNull JsonMessage text) {
        this.kick(text.asComponent());
    }

    /**
     * Kicks the player with a reason.
     *
     * @param message the kick reason
     * @deprecated Use {@link #kick(Component)}
     */
    @Deprecated
    public void kick(@NotNull String message) {
        this.kick(Component.text(message));
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

        if (playerConnection instanceof NettyPlayerConnection) {
            ((NettyPlayerConnection) playerConnection).writeAndFlush(disconnectPacket);
            playerConnection.disconnect();
        } else {
            playerConnection.sendPacket(disconnectPacket);
            playerConnection.refreshOnline(false);
        }
    }

    /**
     * Changes the current held slot for the player.
     *
     * @param slot the slot that the player has to held
     * @throws IllegalArgumentException if {@code slot} is not between 0 and 8
     */
    public void setHeldItemSlot(byte slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "Slot has to be between 0 and 8");

        HeldItemChangePacket heldItemChangePacket = new HeldItemChangePacket();
        heldItemChangePacket.slot = slot;
        playerConnection.sendPacket(heldItemChangePacket);
        refreshHeldSlot(slot);
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
     * Used to get the {@link CustomBlock} that the player is currently mining.
     *
     * @return the currently mined {@link CustomBlock} by the player, null if there is not
     */
    @Nullable
    public CustomBlock getCustomBlockTarget() {
        return targetCustomBlock;
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

        callCancellableEvent(InventoryOpenEvent.class, inventoryOpenEvent, () -> {
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
        UpdateViewPositionPacket updateViewPositionPacket = new UpdateViewPositionPacket();
        updateViewPositionPacket.chunkX = chunkX;
        updateViewPositionPacket.chunkZ = chunkZ;
        playerConnection.sendPacket(updateViewPositionPacket);
    }

    public int getNextTeleportId() {
        return teleportId.getAndIncrement();
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
     * @see Entity#synchronizePosition()
     */
    @Override
    @ApiStatus.Internal
    protected void synchronizePosition() {
        final PlayerPositionAndLookPacket positionAndLookPacket = new PlayerPositionAndLookPacket();
        positionAndLookPacket.position = position.clone();
        positionAndLookPacket.flags = 0x00;
        positionAndLookPacket.teleportId = teleportId.incrementAndGet();
        playerConnection.sendPacket(positionAndLookPacket);

        super.synchronizePosition();
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
     * <a href="https://wiki.vg/Protocol#Player_Abilities_.28clientbound.29">see</a>
     * <p>
     * WARNING: this has nothing to do with {@link CustomBlock#getBreakDelay(Player, BlockPosition, byte, Set)}.
     *
     * @param instantBreak true to allow instant break
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
    @NotNull
    public Map<PlayerStatistic, Integer> getStatisticValueMap() {
        return statisticValueMap;
    }

    /**
     * Gets the player vehicle information.
     *
     * @return the player vehicle information
     */
    @NotNull
    public PlayerVehicleInformation getVehicleInformation() {
        return vehicleInformation;
    }

    /**
     * Sends to the player a {@link PlayerAbilitiesPacket} with all the updated fields.
     */
    protected void refreshAbilities() {
        PlayerAbilitiesPacket playerAbilitiesPacket = new PlayerAbilitiesPacket();
        playerAbilitiesPacket.invulnerable = invulnerable;
        playerAbilitiesPacket.flying = flying;
        playerAbilitiesPacket.allowFlying = allowFlying;
        playerAbilitiesPacket.instantBreak = instantBreak;
        playerAbilitiesPacket.flyingSpeed = flyingSpeed;
        playerAbilitiesPacket.fieldViewModifier = fieldViewModifier;

        playerConnection.sendPacket(playerAbilitiesPacket);
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
        syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);

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
     */
    public @Nullable ItemUpdateStateEvent callItemUpdateStateEvent(boolean allowFood, @Nullable Hand hand) {
        if (hand == null)
            return null;

        final ItemStack updatedItem = getItemInHand(hand);
        final boolean isFood = updatedItem.getMaterial().isFood();

        if (isFood && !allowFood)
            return null;

        ItemUpdateStateEvent itemUpdateStateEvent = new ItemUpdateStateEvent(this, hand, updatedItem);
        callEvent(ItemUpdateStateEvent.class, itemUpdateStateEvent);

        return itemUpdateStateEvent;
    }

    /**
     * Makes the player digging a custom block, see {@link #resetTargetBlock()} to rewind.
     *
     * @param targetCustomBlock   the custom block to dig
     * @param targetBlockPosition the custom block position
     * @param breakers            the breakers of the block, can be null if {@code this} is the only breaker
     */
    public void setTargetBlock(@NotNull CustomBlock targetCustomBlock, @NotNull BlockPosition targetBlockPosition,
                               @Nullable Set<Player> breakers) {
        this.targetCustomBlock = targetCustomBlock;
        this.targetBlockPosition = targetBlockPosition;

        refreshBreakDelay(breakers);
    }

    /**
     * Refreshes the break delay for the next block break stage.
     *
     * @param breakers the list of breakers, can be null if {@code this} is the only breaker
     */
    private void refreshBreakDelay(@Nullable Set<Player> breakers) {
        breakers = breakers == null ? targetBreakers : breakers;

        // Refresh the last tick update
        this.targetBlockBreakCount = 0;

        // Get if multi player breaking is enabled
        final boolean multiPlayerBreaking = targetCustomBlock.enableMultiPlayerBreaking();
        // Get the stage from the custom block object if it is, otherwise use the local field
        final byte stage = multiPlayerBreaking ? targetCustomBlock.getBreakStage(instance, targetBlockPosition) : targetStage;
        // Retrieve the break delay for the current stage
        this.targetBreakDelay = targetCustomBlock.getBreakDelay(this, targetBlockPosition, stage, breakers);
    }

    /**
     * Resets data from the current block the player is mining.
     * If the currently mined block (or if there isn't any) is not a {@link CustomBlock}, nothing happen.
     */
    public void resetTargetBlock() {
        // Remove effect
        PlayerDiggingListener.removeEffect(this);

        if (targetCustomBlock != null) {
            targetCustomBlock.stopDigging(instance, targetBlockPosition, this);
            this.targetCustomBlock = null;
            this.targetBlockPosition = null;
            this.targetBreakDelay = 0;
            this.targetBlockBreakCount = 0;
            this.targetStage = 0;
        }
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

        connection.sendPacket(getEntityType().getSpawnType().getSpawnPacket(this));
        connection.sendPacket(getVelocityPacket());
        connection.sendPacket(getMetadataPacket());
        connection.sendPacket(getEquipmentsPacket());

        if (hasPassenger()) {
            connection.sendPacket(getPassengersPacket());
        }

        // Team
        if (this.getTeam() != null)
            connection.sendPacket(this.getTeam().createTeamsCreationPacket());

        EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
        entityHeadLookPacket.entityId = getEntityId();
        entityHeadLookPacket.yaw = position.getYaw();
        connection.sendPacket(entityHeadLookPacket);
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

    public enum ChatMode {
        ENABLED,
        COMMANDS_ONLY,
        HIDDEN
    }

    public class PlayerSettings {

        private String locale;
        private byte viewDistance;
        private ChatMode chatMode;
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
         */
        public ChatMode getChatMode() {
            return chatMode;
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
         * @param chatMode           the player chat mode
         * @param chatColors         the player chat colors
         * @param displayedSkinParts the player displayed skin parts
         * @param mainHand           the player main hand
         */
        public void refresh(String locale, byte viewDistance, ChatMode chatMode, boolean chatColors,
                            byte displayedSkinParts, MainHand mainHand) {

            final boolean viewDistanceChanged = this.viewDistance != viewDistance;

            this.locale = locale;
            this.viewDistance = viewDistance;
            this.chatMode = chatMode;
            this.chatColors = chatColors;
            this.displayedSkinParts = displayedSkinParts;
            this.mainHand = mainHand;

            metadata.setIndex((byte) 16, Metadata.Byte(displayedSkinParts));

            // Client changed his view distance in the settings
            if (viewDistanceChanged) {
                refreshVisibleChunks();
            }
        }

    }

}
