package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.AdvancementTab;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.bossbar.BossBar;
import net.minestom.server.chat.ChatParser;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.chat.RichMessage;
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
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.JoinGamePacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.permission.Permission;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.resourcepack.ResourcePack;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.stat.PlayerStatistic;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.instance.InstanceUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Those are the major actors of the server,
 * they are not necessary backed by a {@link NettyPlayerConnection} as shown by {@link FakePlayer}
 * <p>
 * You can easily create your own implementation of this and use it with {@link ConnectionManager#setPlayerProvider(PlayerProvider)}.
 */
public class Player extends LivingEntity implements CommandSender {

    private long lastKeepAlive;
    private boolean answerKeepAlive;

    private String username;
    protected final PlayerConnection playerConnection;
    protected final Set<Entity> viewableEntities = new CopyOnWriteArraySet<>();

    private int latency;
    private ColoredText displayName;
    private PlayerSkin skin;

    private DimensionType dimensionType;
    private GameMode gameMode;
    protected final Set<Chunk> viewableChunks = new CopyOnWriteArraySet<>();
    private final AtomicInteger teleportId = new AtomicInteger();

    protected boolean onGround;
    private final ConcurrentLinkedQueue<ClientPlayPacket> packets = new ConcurrentLinkedQueue<>();
    private final boolean levelFlat;
    private final PlayerSettings settings;
    private float exp;
    private int level;

    private final PlayerInventory inventory;
    private Inventory openInventory;
    // Used internally to allow the closing of inventory within the inventory listener
    private boolean didCloseInventory;

    private byte heldSlot;

    private Position respawnPoint;

    private float additionalHearts;
    private int food;
    private float foodSaturation;
    private long startEatingTime;
    private long defaultEatingTime = 1000L;
    private long eatingTime;
    private boolean isEating;

    // Game state (https://wiki.vg/Protocol#Change_Game_State)
    private boolean enableRespawnScreen;

    // CustomBlock break delay
    private CustomBlock targetCustomBlock;
    private BlockPosition targetBlockPosition;
    private long targetBreakDelay; // The last break delay requested
    private long targetBlockBreakCount; // Number of tick since the last stage change
    private byte targetStage; // The current stage of the target block, only if multi player breaking is disabled
    private final Set<Player> targetBreakers = new HashSet<>(1); // Only used if multi player breaking is disabled, contains only this player

    private BelowNameTag belowNameTag;

    private int permissionLevel;

    private boolean reducedDebugScreenInformation;

    // Abilities
    private boolean flying;
    private boolean allowFlying;
    private boolean instantBreak;
    private float flyingSpeed = 0.05f;
    private float walkingSpeed = 0.1f;

    // Statistics
    private final Map<PlayerStatistic, Integer> statisticValueMap = new Hashtable<>();

    // Vehicle
    private final PlayerVehicleInformation vehicleInformation = new PlayerVehicleInformation();

    // Tick related
    private final PlayerTickEvent playerTickEvent = new PlayerTickEvent(this);

    private final List<Permission> permissions = new LinkedList<>();

    public Player(UUID uuid, String username, PlayerConnection playerConnection) {
        super(EntityType.PLAYER);
        this.uuid = uuid; // Override Entity#uuid defined in the constructor
        this.username = username;
        this.playerConnection = playerConnection;

        setBoundingBox(0.69f, 1.8f, 0.69f);

        setRespawnPoint(new Position(0, 0, 0));

        this.settings = new PlayerSettings();
        this.inventory = new PlayerInventory(this);

        setCanPickupItem(true); // By default

        // Allow the server to send the next keep alive packet
        refreshAnswerKeepAlive(true);

        this.gameMode = GameMode.SURVIVAL;
        this.dimensionType = DimensionType.OVERWORLD;
        this.levelFlat = true;
        refreshPosition(0, 0, 0);

        // Used to cache the breaker for single custom block breaking
        this.targetBreakers.add(this);

        // FakePlayer init its connection there
        playerConnectionInit();

        MinecraftServer.getEntityManager().addWaitingPlayer(this);
    }

    /**
     * Used when the player is created.
     * Init the player and spawn him.
     * <p>
     * WARNING: executed in the main update thread
     */
    protected void init() {
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
        spawnPositionPacket.x = 0;
        spawnPositionPacket.y = 0;
        spawnPositionPacket.z = 0;
        playerConnection.sendPacket(spawnPositionPacket);

        // Add player to list with spawning skin
        PlayerSkinInitEvent skinInitEvent = new PlayerSkinInitEvent(this);
        callEvent(PlayerSkinInitEvent.class, skinInitEvent);
        this.skin = skinInitEvent.getSkin();
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

        // Send server tags
        TagsPacket tags = new TagsPacket();
        TagManager tagManager = MinecraftServer.getTagManager();
        tagManager.addRequiredTagsToPacket(tags);

        UpdateTagListEvent event = new UpdateTagListEvent(tags);
        callEvent(UpdateTagListEvent.class, event);

        getPlayerConnection().sendPacket(tags);

        // Some client update
        playerConnection.sendPacket(getPropertiesPacket()); // Send default properties
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
    public float getAttributeValue(@NotNull Attribute attribute) {
        if (attribute == Attribute.MOVEMENT_SPEED) {
            return walkingSpeed;
        }
        return super.getAttributeValue(attribute);
    }

    @Override
    public void update(long time) {
        // Flush all pending packets
        playerConnection.flush();

        playerConnection.updateStats();

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
                    final boolean canContinue = this.targetCustomBlock.processStage(instance, targetBlockPosition, this, stageIncrease);
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
                        final BlockBreakAnimationPacket blockBreakAnimationPacket = new BlockBreakAnimationPacket(entityId, targetBlockPosition, targetStage);
                        Check.notNull(chunk, "Tried to interact with an unloaded chunk.");
                        chunk.sendPacketToViewers(blockBreakAnimationPacket);

                        refreshBreakDelay(targetBreakers);
                        this.targetStage += stageIncrease;
                    }
                }
            }
        }

        // Experience orb pickup
        final Chunk chunk = instance.getChunkAt(getPosition()); // TODO check surrounding chunks
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

        // Eating animation
        if (isEating()) {
            if (time - startEatingTime >= eatingTime) {
                refreshEating(false);

                triggerStatus((byte) 9); // Mark item use as finished
                ItemUpdateStateEvent itemUpdateStateEvent = callItemUpdateStateEvent(true);

                Check.notNull(itemUpdateStateEvent, "#callItemUpdateStateEvent returned null.");

                // Refresh hand
                final boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
                refreshActiveHand(false, isOffHand, false);

                final ItemStack foodItem = itemUpdateStateEvent.getItemStack();
                final boolean isFood = foodItem.getMaterial().isFood();

                if (isFood) {
                    PlayerEatEvent playerEatEvent = new PlayerEatEvent(this, foodItem);
                    callEvent(PlayerEatEvent.class, playerEatEvent);
                }
            }
        }

        // Tick event
        callEvent(PlayerTickEvent.class, playerTickEvent);

        // Multiplayer sync
        final boolean positionChanged = position.getX() != lastX || position.getY() != lastY || position.getZ() != lastZ;
        final boolean viewChanged = position.getYaw() != lastYaw || position.getPitch() != lastPitch;
        if (!getViewers().isEmpty() && (positionChanged || viewChanged)) {
            ServerPacket updatePacket;
            ServerPacket optionalUpdatePacket = null;
            if (positionChanged && viewChanged) {
                EntityPositionAndRotationPacket entityPositionAndRotationPacket = new EntityPositionAndRotationPacket();
                entityPositionAndRotationPacket.entityId = getEntityId();
                entityPositionAndRotationPacket.deltaX = (short) ((position.getX() * 32 - lastX * 32) * 128);
                entityPositionAndRotationPacket.deltaY = (short) ((position.getY() * 32 - lastY * 32) * 128);
                entityPositionAndRotationPacket.deltaZ = (short) ((position.getZ() * 32 - lastZ * 32) * 128);
                entityPositionAndRotationPacket.yaw = position.getYaw();
                entityPositionAndRotationPacket.pitch = position.getPitch();
                entityPositionAndRotationPacket.onGround = onGround;

                lastX = position.getX();
                lastY = position.getY();
                lastZ = position.getZ();
                lastYaw = position.getYaw();
                lastPitch = position.getPitch();
                updatePacket = entityPositionAndRotationPacket;
            } else if (positionChanged) {
                EntityPositionPacket entityPositionPacket = new EntityPositionPacket();
                entityPositionPacket.entityId = getEntityId();
                entityPositionPacket.deltaX = (short) ((position.getX() * 32 - lastX * 32) * 128);
                entityPositionPacket.deltaY = (short) ((position.getY() * 32 - lastY * 32) * 128);
                entityPositionPacket.deltaZ = (short) ((position.getZ() * 32 - lastZ * 32) * 128);
                entityPositionPacket.onGround = onGround;
                lastX = position.getX();
                lastY = position.getY();
                lastZ = position.getZ();
                updatePacket = entityPositionPacket;
            } else {
                // View changed
                EntityRotationPacket entityRotationPacket = new EntityRotationPacket();
                entityRotationPacket.entityId = getEntityId();
                entityRotationPacket.yaw = position.getYaw();
                entityRotationPacket.pitch = position.getPitch();
                entityRotationPacket.onGround = onGround;

                lastYaw = position.getYaw();
                lastPitch = position.getPitch();
                updatePacket = entityRotationPacket;
            }

            if (viewChanged) {
                EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
                entityHeadLookPacket.entityId = getEntityId();
                entityHeadLookPacket.yaw = position.getYaw();
                optionalUpdatePacket = entityHeadLookPacket;
            }

            // Send the update packet
            if (optionalUpdatePacket != null) {
                sendPacketsToViewers(updatePacket, optionalUpdatePacket);
            } else {
                sendPacketToViewers(updatePacket);
            }

        }

    }

    @Override
    public void kill() {
        if (!isDead()) {
            // send death screen text to the killed player
            {
                ColoredText deathText;
                if (lastDamageSource != null) {
                    deathText = lastDamageSource.buildDeathScreenText(this);
                } else { // may happen if killed by the server without applying damage
                    deathText = ColoredText.of("Killed by poor programming.");
                }

                // #buildDeathScreenText can return null, check here
                if (deathText != null) {
                    CombatEventPacket deathPacket = CombatEventPacket.death(this, Optional.empty(), deathText);
                    playerConnection.sendPacket(deathPacket);
                }
            }

            // send death message to chat
            {
                JsonMessage chatMessage;
                if (lastDamageSource != null) {
                    chatMessage = lastDamageSource.buildDeathMessage(this);
                } else { // may happen if killed by the server without applying damage
                    chatMessage = ColoredText.of(getUsername() + " was killed by poor programming.");
                }

                // #buildDeathMessage can return null, check here
                if (chatMessage != null) {
                    MinecraftServer.getConnectionManager().broadcastMessage(chatMessage);
                }
            }
        }
        super.kill();
    }

    /**
     * Respawns the player by sending a {@link RespawnPacket} to the player and teleporting him
     * to {@link #getRespawnPoint()}. It also resets fire and his health
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
        callEvent(PlayerDisconnectEvent.class, new PlayerDisconnectEvent(this));

        super.remove();
        this.packets.clear();
        if (getOpenInventory() != null)
            getOpenInventory().removeViewer(this);

        // Boss bars cache
        {
            Set<BossBar> bossBars = BossBar.getBossBars(this);
            if (bossBars != null) {
                for (BossBar bossBar : bossBars) {
                    bossBar.removeViewer(this);
                }
            }
        }

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
    public boolean addViewer(@NotNull Player player) {
        if (player == this)
            return false;

        final boolean result = super.addViewer(player);
        if (!result)
            return false;

        PlayerConnection viewerConnection = player.getPlayerConnection();
        showPlayer(viewerConnection);
        return true;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        if (player == this)
            return false;

        boolean result = super.removeViewer(player);
        PlayerConnection viewerConnection = player.getPlayerConnection();
        viewerConnection.sendPacket(getRemovePlayerToList());

        // Team
        if (this.getTeam() != null && this.getTeam().getMembers().size() == 1) // If team only contains "this" player
            viewerConnection.sendPacket(this.getTeam().createTeamDestructionPacket());
        return result;
    }

    @Override
    public void setInstance(@NotNull Instance instance) {
        Check.notNull(instance, "instance cannot be null!");
        Check.argCondition(this.instance == instance, "Instance should be different than the current one");

        final boolean firstSpawn = this.instance == null; // TODO: Handle player reconnections, must be false in that case too

        // true if the chunks need to be send to the client, can be false if the instances share the same chunks (eg SharedInstance)
        final boolean needWorldRefresh = !InstanceUtils.areLinked(this.instance, instance);

        if (needWorldRefresh) {
            // Remove all previous viewable chunks (from the previous instance)
            for (Chunk viewableChunk : viewableChunks) {
                viewableChunk.removeViewer(this);
            }

            if (this.instance != null) {
                final DimensionType instanceDimensionType = instance.getDimensionType();
                if (dimensionType != instanceDimensionType)
                    sendDimension(instanceDimensionType);
            }

            final long[] visibleChunks = ChunkUtils.getChunksInRange(position, getChunkRange());
            final int length = visibleChunks.length;

            AtomicInteger counter = new AtomicInteger(0);
            for (long visibleChunk : visibleChunks) {
                final int chunkX = ChunkUtils.getChunkCoordX(visibleChunk);
                final int chunkZ = ChunkUtils.getChunkCoordZ(visibleChunk);

                final ChunkCallback callback = (chunk) -> {
                    if (chunk != null) {
                        chunk.addViewer(this);
                        if (chunk.getChunkX() == Math.floorDiv((int) getPosition().getX(), 16) && chunk.getChunkZ() == Math.floorDiv((int) getPosition().getZ(), 16))
                            updateViewPosition(chunk);
                    }
                    final boolean isLast = counter.get() == length - 1;
                    if (isLast) {
                        // This is the last chunk to be loaded , spawn player
                        spawnPlayer(instance, firstSpawn);
                    } else {
                        // Increment the counter of current loaded chunks
                        counter.incrementAndGet();
                    }
                };

                // WARNING: if auto load is disabled and no chunks are loaded beforehand, player will be stuck.
                instance.loadOptionalChunk(chunkX, chunkZ, callback);
            }
        } else {
            spawnPlayer(instance, firstSpawn);
        }
    }

    /**
     * Used to spawn the player once the client has all the required chunks.
     * <p>
     * Does add the player to {@code instance}, remove all viewable entities and call {@link PlayerSpawnEvent}.
     * <p>
     * UNSAFE: only called with {@link #setInstance(Instance)}.
     *
     * @param firstSpawn true if this is the player first spawn
     */
    private void spawnPlayer(Instance instance, boolean firstSpawn) {
        this.viewableEntities.forEach(entity -> entity.removeViewer(this));
        super.setInstance(instance);
        PlayerSpawnEvent spawnEvent = new PlayerSpawnEvent(this, instance, firstSpawn);
        callEvent(PlayerSpawnEvent.class, spawnEvent);
    }

    @NotNull
    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 14);
            fillMetadataIndex(packet, 16);
        };
    }

    @Override
    protected void fillMetadataIndex(@NotNull BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 14) {
            packet.writeByte((byte) 14);
            packet.writeByte(METADATA_FLOAT);
            packet.writeFloat(additionalHearts);
        } else if (index == 16) {
            packet.writeByte((byte) 16);
            packet.writeByte(METADATA_BYTE);
            packet.writeByte(getSettings().getDisplayedSkinParts());
        }
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
     *
     * @param channel the message channel
     * @param message the message
     */
    public void sendPluginMessage(@NotNull String channel, @NotNull String message) {
        // Write the data
        BinaryWriter writer = new BinaryWriter();
        writer.writeSizedString(message);
        // Retrieve the data
        final byte[] data = writer.toByteArray();

        sendPluginMessage(channel, data);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        sendMessage(ColoredText.of(message));
    }

    @NotNull
    @Override
    public Collection<Permission> getAllPermissions() {
        return permissions;
    }

    /**
     * Sends a message to the player.
     *
     * @param message the message to send,
     *                you can use {@link ColoredText} and/or {@link RichMessage} to create it easily
     */
    public void sendMessage(@NotNull JsonMessage message) {
        sendJsonMessage(message.toString());
    }

    /**
     * Sends a legacy message with the specified color char.
     *
     * @param text      the text with the legacy color formatting
     * @param colorChar the color character
     */
    public void sendLegacyMessage(@NotNull String text, char colorChar) {
        ColoredText coloredText = ColoredText.ofLegacy(text, colorChar);
        sendJsonMessage(coloredText.toString());
    }

    /**
     * Sends a legacy message with the default color char {@link ChatParser#COLOR_CHAR}.
     *
     * @param text the text with the legacy color formatting
     */
    public void sendLegacyMessage(@NotNull String text) {
        ColoredText coloredText = ColoredText.ofLegacy(text, ChatParser.COLOR_CHAR);
        sendJsonMessage(coloredText.toString());
    }

    public void sendJsonMessage(@NotNull String json) {
        ChatMessagePacket chatMessagePacket =
                new ChatMessagePacket(json, ChatMessagePacket.Position.CHAT);
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
     * Plays a sound from the {@link Sound} enum.
     *
     * @param sound         the sound to play
     * @param soundCategory the sound category
     * @param x             the effect X
     * @param y             the effect Y
     * @param z             the effect Z
     * @param volume        the volume of the sound (1 is 100%)
     * @param pitch         the pitch of the sound, between 0.5 and 2.0
     */
    public void playSound(@NotNull Sound sound, @NotNull SoundCategory soundCategory, int x, int y, int z, float volume, float pitch) {
        SoundEffectPacket soundEffectPacket = new SoundEffectPacket();
        soundEffectPacket.soundId = sound.getId();
        soundEffectPacket.soundCategory = soundCategory;
        soundEffectPacket.x = x * 8;
        soundEffectPacket.y = y * 8;
        soundEffectPacket.z = z * 8;
        soundEffectPacket.volume = volume;
        soundEffectPacket.pitch = pitch;
        playerConnection.sendPacket(soundEffectPacket);
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
     */
    public void playSound(@NotNull String identifier, @NotNull SoundCategory soundCategory, int x, int y, int z, float volume, float pitch) {
        NamedSoundEffectPacket namedSoundEffectPacket = new NamedSoundEffectPacket();
        namedSoundEffectPacket.soundName = identifier;
        namedSoundEffectPacket.soundCategory = soundCategory;
        namedSoundEffectPacket.x = x * 8;
        namedSoundEffectPacket.y = y * 8;
        namedSoundEffectPacket.z = z * 8;
        namedSoundEffectPacket.volume = volume;
        namedSoundEffectPacket.pitch = pitch;
        playerConnection.sendPacket(namedSoundEffectPacket);
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
     */
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
     */
    public void sendHeaderFooter(@Nullable ColoredText header, @Nullable ColoredText footer) {
        PlayerListHeaderAndFooterPacket playerListHeaderAndFooterPacket = new PlayerListHeaderAndFooterPacket();
        playerListHeaderAndFooterPacket.emptyHeader = header == null;
        playerListHeaderAndFooterPacket.emptyFooter = footer == null;
        playerListHeaderAndFooterPacket.header = header;
        playerListHeaderAndFooterPacket.footer = footer;

        playerConnection.sendPacket(playerListHeaderAndFooterPacket);
    }

    /**
     * Common method to send a title.
     *
     * @param text   the text of the title
     * @param action the action of the title (where to show it)
     * @see #sendTitleTime(int, int, int) to specify the display time
     */
    private void sendTitle(@NotNull ColoredText text, @NotNull TitlePacket.Action action) {
        TitlePacket titlePacket = new TitlePacket();
        titlePacket.action = action;

        switch (action) {
            case SET_TITLE:
                titlePacket.titleText = text;
                break;
            case SET_SUBTITLE:
                titlePacket.subtitleText = text;
                break;
            case SET_ACTION_BAR:
                titlePacket.actionBarText = text;
                break;
            default:
                throw new UnsupportedOperationException("Invalid TitlePacket.Action type!");
        }

        playerConnection.sendPacket(titlePacket);
    }

    /**
     * Sends a title and subtitle message.
     *
     * @param title    the title message
     * @param subtitle the subtitle message
     * @see #sendTitleTime(int, int, int) to specify the display time
     */
    public void sendTitleSubtitleMessage(@NotNull ColoredText title, @NotNull ColoredText subtitle) {
        sendTitle(title, TitlePacket.Action.SET_TITLE);
        sendTitle(subtitle, TitlePacket.Action.SET_SUBTITLE);
    }

    /**
     * Sends a title message.
     *
     * @param title the title message
     * @see #sendTitleTime(int, int, int) to specify the display time
     */
    public void sendTitleMessage(@NotNull ColoredText title) {
        sendTitle(title, TitlePacket.Action.SET_TITLE);
    }

    /**
     * Sends a subtitle message.
     *
     * @param subtitle the subtitle message
     * @see #sendTitleTime(int, int, int) to specify the display time
     */
    public void sendSubtitleMessage(@NotNull ColoredText subtitle) {
        sendTitle(subtitle, TitlePacket.Action.SET_SUBTITLE);
    }

    /**
     * Sends an action bar message.
     *
     * @param actionBar the action bar message
     * @see #sendTitleTime(int, int, int) to specify the display time
     */
    public void sendActionBarMessage(@NotNull ColoredText actionBar) {
        sendTitle(actionBar, TitlePacket.Action.SET_ACTION_BAR);
    }

    /**
     * Specifies the display time of a title.
     *
     * @param fadeIn  ticks to spend fading in
     * @param stay    ticks to keep the title displayed
     * @param fadeOut ticks to spend out, not when to start fading out
     */
    public void sendTitleTime(int fadeIn, int stay, int fadeOut) {
        TitlePacket titlePacket = new TitlePacket();
        titlePacket.action = TitlePacket.Action.SET_TIMES_AND_DISPLAY;
        titlePacket.fadeIn = fadeIn;
        titlePacket.stay = stay;
        titlePacket.fadeOut = fadeOut;
        playerConnection.sendPacket(titlePacket);
    }

    /**
     * Hides the previous title.
     */
    public void hideTitle() {
        TitlePacket titlePacket = new TitlePacket();
        titlePacket.action = TitlePacket.Action.HIDE;
        playerConnection.sendPacket(titlePacket);
    }

    /**
     * Resets the previous title.
     */
    public void resetTitle() {
        TitlePacket titlePacket = new TitlePacket();
        titlePacket.action = TitlePacket.Action.RESET;
        playerConnection.sendPacket(titlePacket);
    }

    @Override
    public boolean isImmune(@NotNull DamageType type) {
        if (!getGameMode().canTakeDamage()) {
            return type != DamageType.VOID;
        }
        return super.isImmune(type);
    }

    @Override
    public void setAttribute(@NotNull Attribute attribute, float value) {
        super.setAttribute(attribute, value);
        if (playerConnection != null)
            playerConnection.sendPacket(getPropertiesPacket());
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
        return additionalHearts;
    }

    /**
     * Updates the internal field and send the appropriate {@link EntityMetaDataPacket}.
     *
     * @param additionalHearts the count of additional hearts
     */
    public void setAdditionalHearts(float additionalHearts) {
        this.additionalHearts = additionalHearts;
        sendMetadataIndex(14);
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
     */
    public void setFood(int food) {
        Check.argCondition(!MathUtils.isBetween(food, 0, 20), "Food has to be between 0 and 20");
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
     */
    public void setFoodSaturation(float foodSaturation) {
        Check.argCondition(!MathUtils.isBetween(foodSaturation, 0, 5), "Food saturation has to be between 0 and 5");
        this.foodSaturation = foodSaturation;
        sendUpdateHealthPacket();
    }

    /**
     * Gets if the player is eating.
     *
     * @return true if the player is eating, false otherwise
     */
    public boolean isEating() {
        return isEating;
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
     * @return the player display name,
     * null means that {@link #getUsername()} is displayed
     */
    @Nullable
    public ColoredText getDisplayName() {
        return displayName;
    }

    /**
     * Changes the player display name in the tab-list.
     * <p>
     * Sets to null to show the player username.
     *
     * @param displayName the display name, null to display the username
     */
    public void setDisplayName(@Nullable ColoredText displayName) {
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

        for (Player viewer : getViewers()) {
            final PlayerConnection connection = viewer.getPlayerConnection();

            connection.sendPacket(removePlayerPacket);
            connection.sendPacket(destroyEntitiesPacket);

            showPlayer(connection);
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
     * Gets the player username.
     *
     * @return the player username
     */
    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * Changes the internal player name, used for the {@link PlayerPreLoginEvent}
     * mostly unsafe outside of it.
     *
     * @param username the new player name
     */
    protected void setUsername(@NotNull String username) {
        this.username = username;
    }

    private void sendChangeGameStatePacket(@NotNull ChangeGameStatePacket.Reason reason, float value) {
        ChangeGameStatePacket changeGameStatePacket = new ChangeGameStatePacket();
        changeGameStatePacket.reason = reason;
        changeGameStatePacket.value = value;
        playerConnection.sendPacket(changeGameStatePacket);
    }

    /**
     * Calls an {@link ItemDropEvent} with a specified item.
     *
     * @param item the item to drop
     * @return true if player can drop the item (event not cancelled), false otherwise
     */
    public boolean dropItem(@NotNull ItemStack item) {
        ItemDropEvent itemDropEvent = new ItemDropEvent(item);
        callEvent(ItemDropEvent.class, itemDropEvent);
        return !itemDropEvent.isCancelled();
    }

    /**
     * Sets the player resource pack.
     *
     * @param resourcePack the resource pack
     */
    public void setResourcePack(ResourcePack resourcePack) {
        Check.notNull(resourcePack, "The resource pack cannot be null");
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

    private void facePosition(@NotNull FacePoint facePoint, @NotNull Position targetPosition, @Nullable Entity entity, @Nullable FacePoint targetPoint) {
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
     * @return the default respawn point
     */
    @NotNull
    public Position getRespawnPoint() {
        return respawnPoint;
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
        getInventory().update();

        SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
        spawnPlayerPacket.entityId = getEntityId();
        spawnPlayerPacket.playerUuid = getUuid();
        spawnPlayerPacket.position = getPosition();
        sendPacketToViewers(spawnPlayerPacket);

        // Update for viewers
        sendPacketToViewersAndSelf(getVelocityPacket());
        sendPacketToViewersAndSelf(getMetadataPacket());
        playerConnection.sendPacket(getPropertiesPacket());
        syncEquipments();

        {
            // Send new chunks
            final BlockPosition pos = position.toBlockPosition();
            final Chunk chunk = instance.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            Check.notNull(chunk, "Tried to interact with an unloaded chunk.");
            onChunkChange(chunk);
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
     * <p>
     * It does remove and add the player from the chunks viewers list when removed or added.
     * It also calls the events {@link PlayerChunkUnloadEvent} and {@link PlayerChunkLoadEvent}.
     *
     * @param newChunk the current/new player chunk
     */
    protected void onChunkChange(@NotNull Chunk newChunk) {
        // Previous chunks indexes
        final long[] lastVisibleChunks = viewableChunks.stream().mapToLong(viewableChunks ->
                ChunkUtils.getChunkIndex(viewableChunks.getChunkX(), viewableChunks.getChunkZ())
        ).toArray();

        // New chunks indexes
        final long[] updatedVisibleChunks = ChunkUtils.getChunksInRange(new Position(16 * newChunk.getChunkX(), 0, 16 * newChunk.getChunkZ()), getChunkRange());

        // Find the difference between the two arrays¬
        final int[] oldChunks = ArrayUtils.getDifferencesBetweenArray(lastVisibleChunks, updatedVisibleChunks);
        final int[] newChunks = ArrayUtils.getDifferencesBetweenArray(updatedVisibleChunks, lastVisibleChunks);

        // Unload old chunks
        for (int index : oldChunks) {
            final long chunkIndex = lastVisibleChunks[index];
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);

            UnloadChunkPacket unloadChunkPacket = new UnloadChunkPacket();
            unloadChunkPacket.chunkX = chunkX;
            unloadChunkPacket.chunkZ = chunkZ;
            playerConnection.sendPacket(unloadChunkPacket);

            final Chunk chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk != null)
                chunk.removeViewer(this);
        }

        updateViewPosition(newChunk);

        // Load new chunks
        for (int index : newChunks) {
            final long chunkIndex = updatedVisibleChunks[index];
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);

            instance.loadOptionalChunk(chunkX, chunkZ, chunk -> {
                if (chunk == null) {
                    // Cannot load chunk (auto load is not enabled)
                    return;
                }
                chunk.addViewer(this);
            });
        }
    }

    @Override
    public void teleport(@NotNull Position position, @Nullable Runnable callback) {
        super.teleport(position, () -> {
            updatePlayerPosition();
            OptionalCallback.execute(callback);
        });
    }

    @Override
    public void teleport(@NotNull Position position) {
        teleport(position, null);
    }

    /**
     * Gets the player connection.
     * <p>
     * Used to send packets and get relatives stuff to the connection.
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
        Check.notNull(gameMode, "GameMode cannot be null");
        this.gameMode = gameMode;
        sendChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, gameMode.getId());

        PlayerInfoPacket infoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE);
        infoPacket.playerInfos.add(new PlayerInfoPacket.UpdateGamemode(getUuid(), gameMode));
        sendPacketToViewersAndSelf(infoPacket);
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
        Check.notNull(dimensionType, "Dimension cannot be null!");
        Check.argCondition(dimensionType.equals(getDimensionType()), "The dimension needs to be different than the current one!");

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
     */
    public void kick(@NotNull ColoredText text) {
        DisconnectPacket disconnectPacket = new DisconnectPacket();
        disconnectPacket.message = text;
        playerConnection.sendPacket(disconnectPacket);
        playerConnection.disconnect();
        playerConnection.refreshOnline(false);
    }

    /**
     * Kicks the player with a reason.
     *
     * @param message the kick reason
     */
    public void kick(@NotNull String message) {
        kick(ColoredText.of(message));
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
        if (team != null)
            getPlayerConnection().sendPacket(team.getTeamsCreationPacket());
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
     * Gets if the player is sneaking.
     * <p>
     * WARNING: this can be bypassed by hacked client, this is only what the client told the server.
     *
     * @return true if the player is sneaking
     */
    public boolean isSneaking() {
        return crouched;
    }

    /**
     * Gets if the player is sprinting.
     * <p>
     * WARNING: this can be bypassed by hacked client, this is only what the client told the server.
     *
     * @return true if the player is sprinting
     */
    public boolean isSprinting() {
        return sprinting;
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
        Check.notNull(inventory, "Inventory cannot be null, use Player#closeInventory() to close current");

        InventoryOpenEvent inventoryOpenEvent = new InventoryOpenEvent(this, inventory);

        callCancellableEvent(InventoryOpenEvent.class, inventoryOpenEvent, () -> {

            if (getOpenInventory() != null) {
                closeInventory();
            }

            Inventory newInventory = inventoryOpenEvent.getInventory();

            if (newInventory == null) {
                // just close the inventory
                return;
            }

            OpenWindowPacket openWindowPacket = new OpenWindowPacket();
            openWindowPacket.windowId = newInventory.getWindowId();
            openWindowPacket.windowType = newInventory.getInventoryType().getWindowType();
            openWindowPacket.title = newInventory.getTitle();
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
            getInventory().setCursorItem(ItemStack.getAirItem());
        } else {
            cursorItem = openInventory.getCursorItem(this);
            openInventory.setCursorItem(this, ItemStack.getAirItem());
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
     * @param chunk the chunk to update the view
     */
    public void updateViewPosition(@NotNull Chunk chunk) {
        UpdateViewPositionPacket updateViewPositionPacket = new UpdateViewPositionPacket();
        updateViewPositionPacket.chunkX = chunk.getChunkX();
        updateViewPositionPacket.chunkZ = chunk.getChunkZ();
        playerConnection.sendPacket(updateViewPositionPacket);
    }

    /**
     * Used for synchronization purpose, mainly for teleportation
     */
    protected void updatePlayerPosition() {
        PlayerPositionAndLookPacket positionAndLookPacket = new PlayerPositionAndLookPacket();
        positionAndLookPacket.position = position.clone(); // clone needed to prevent synchronization issue
        positionAndLookPacket.flags = 0x00;
        positionAndLookPacket.teleportId = teleportId.incrementAndGet();
        playerConnection.sendPacket(positionAndLookPacket);
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

    public float getWalkingSpeed() {
        return walkingSpeed;
    }

    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
        refreshAbilities();
    }

    /**
     * This is the map used to send the statistic packet.
     * It is possible to add/remove/change statistic value directly into it.
     *
     * @return the modifiable statistic map
     */
    public Map<PlayerStatistic, Integer> getStatisticValueMap() {
        return statisticValueMap;
    }

    /**
     * Gets the player vehicle information.
     *
     * @return the player vehicle information
     */
    public PlayerVehicleInformation getVehicleInformation() {
        return vehicleInformation;
    }

    /**
     * Sends to the player a {@link PlayerAbilitiesPacket} with all the updated fields
     * (walkingSpeed set to 0.1).
     */
    protected void refreshAbilities() {
        PlayerAbilitiesPacket playerAbilitiesPacket = new PlayerAbilitiesPacket();
        playerAbilitiesPacket.invulnerable = invulnerable;
        playerAbilitiesPacket.flying = flying;
        playerAbilitiesPacket.allowFlying = allowFlying;
        playerAbilitiesPacket.instantBreak = instantBreak;
        playerAbilitiesPacket.flyingSpeed = flyingSpeed;
        playerAbilitiesPacket.walkingSpeed = 0.1f;

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

        refreshEating(false);
    }

    public void refreshEating(boolean isEating, long eatingTime) {
        this.isEating = isEating;
        if (isEating) {
            this.startEatingTime = System.currentTimeMillis();
            this.eatingTime = eatingTime;
        } else {
            this.startEatingTime = 0;
        }
    }

    public void refreshEating(boolean isEating) {
        refreshEating(isEating, defaultEatingTime);
    }

    /**
     * Used to call {@link ItemUpdateStateEvent} with the proper item
     * It does check which hand to get the item to update.
     *
     * @param allowFood true if food should be updated, false otherwise
     * @return the called {@link ItemUpdateStateEvent},
     * null if there is no item to update the state
     */
    @Nullable
    public ItemUpdateStateEvent callItemUpdateStateEvent(boolean allowFood) {
        final Material mainHandMat = getItemInMainHand().getMaterial();
        final Material offHandMat = getItemInOffHand().getMaterial();
        final boolean isOffhand = offHandMat.hasState();

        final ItemStack updatedItem = isOffhand ? getItemInOffHand() :
                mainHandMat.hasState() ? getItemInMainHand() : null;
        if (updatedItem == null) // No item with state, cancel
            return null;

        final boolean isFood = updatedItem.getMaterial().isFood();

        if (isFood && !allowFood)
            return null;

        final Hand hand = isOffhand ? Hand.OFF : Hand.MAIN;
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
    public void setTargetBlock(@NotNull CustomBlock targetCustomBlock, @NotNull BlockPosition targetBlockPosition, @Nullable Set<Player> breakers) {
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
        final int serverRange = MinecraftServer.getChunkViewDistance();
        final int playerRange = getSettings().viewDistance;
        if (playerRange == 0) {
            return serverRange; // Didn't receive settings packet yet (is the case on login)
        } else {
            return Math.min(playerRange, serverRange);
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
        SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
        spawnPlayerPacket.entityId = getEntityId();
        spawnPlayerPacket.playerUuid = getUuid();
        spawnPlayerPacket.position = getPosition();

        connection.sendPacket(getAddPlayerToList());

        connection.sendPacket(spawnPlayerPacket);
        connection.sendPacket(getVelocityPacket());
        connection.sendPacket(getMetadataPacket());

        // Equipments synchronization
        syncEquipments(connection);

        if (hasPassenger()) {
            connection.sendPacket(getPassengersPacket());
        }

        // Team
        if (this.getTeam() != null)
            connection.sendPacket(this.getTeam().getTeamsCreationPacket());

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
        public void refresh(String locale, byte viewDistance, ChatMode chatMode, boolean chatColors, byte displayedSkinParts, MainHand mainHand) {
            this.locale = locale;
            this.viewDistance = viewDistance;
            this.chatMode = chatMode;
            this.chatColors = chatColors;
            this.displayedSkinParts = displayedSkinParts;
            this.mainHand = mainHand;
            sendMetadataIndex(16);
        }

    }

}
