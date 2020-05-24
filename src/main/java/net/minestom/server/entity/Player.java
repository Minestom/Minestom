package net.minestom.server.entity;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.bossbar.BossBar;
import net.minestom.server.chat.Chat;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.property.Attribute;
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
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.scoreboard.BelowNameScoreboard;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.stat.PlayerStatistic;
import net.minestom.server.utils.*;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Dimension;
import net.minestom.server.world.LevelType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Player extends LivingEntity {

    private long lastKeepAlive;

    private String username;
    private PlayerConnection playerConnection;
    private ConcurrentLinkedQueue<ClientPlayPacket> packets = new ConcurrentLinkedQueue<>();

    private int latency;

    private Dimension dimension;
    private GameMode gameMode;
    private LevelType levelType;
    private int teleportId = 0;

    protected boolean onGround;

    protected Set<Entity> viewableEntities = new CopyOnWriteArraySet<>();
    protected Set<Chunk> viewableChunks = new CopyOnWriteArraySet<>();

    private PlayerSettings settings;
    private float exp;
    private int level;
    private PlayerInventory inventory;
    private short heldSlot;
    private Inventory openInventory;

    private Position respawnPoint;

    private int food;
    private float foodSaturation;
    private long startEatingTime;
    private long defaultEatingTime = 1000L;
    private long eatingTime;
    private boolean isEating;

    // CustomBlock break delay
    private CustomBlock targetCustomBlock;
    private BlockPosition targetBlockPosition;
    private long targetBlockTime;
    private byte targetLastStage;
    private int blockBreakTime;

    private Set<BossBar> bossBars = new CopyOnWriteArraySet<>();
    private Team team;
    private BelowNameScoreboard belowNameScoreboard;

    /**
     * Last damage source to hit this player, used to display the death message.
     */
    private DamageType lastDamageSource;

    private int permissionLevel;

    private boolean reducedDebugScreenInformation;

    // Abilities
    private boolean invulnerable;
    private boolean flying;
    private boolean allowFlying;
    private boolean instantBreak;
    private float flyingSpeed = 0.05f;
    private float fieldViewModifier = 0.1f;

    // Statistics
    private Map<PlayerStatistic, Integer> statisticValueMap = new Hashtable<>();

    // Vehicle
    private PlayerVehicleInformation vehicleInformation = new PlayerVehicleInformation();

    public Player(UUID uuid, String username, PlayerConnection playerConnection) {
        super(EntityType.PLAYER.getId());
        this.uuid = uuid;
        this.username = username;
        this.playerConnection = playerConnection;

        setBoundingBox(0.69f, 1.8f, 0.69f);

        setRespawnPoint(new Position(0, 0, 0));

        // Some client update
        getPlayerConnection().sendPacket(getPropertiesPacket()); // Send default properties
        refreshHealth();
        refreshAbilities();

        this.settings = new PlayerSettings();
        this.inventory = new PlayerInventory(this);

        setCanPickupItem(true); // By default
    }

    @Override
    public boolean damage(DamageType type, float value) {
        boolean result = super.damage(type, value);
        if (result) {
            lastDamageSource = type;
        }
        return result;
    }

    @Override
    public void update() {

        // Flush all pending packets
        playerConnection.flush();

        // Process received packets
        ClientPlayPacket packet;
        while ((packet = packets.poll()) != null) {
            packet.process(this);
        }

        super.update(); // Super update (item pickup)

        // Target block stage
        if (targetCustomBlock != null) {
            int animationCount = 10;
            long since = System.currentTimeMillis() - targetBlockTime;
            byte stage = (byte) (since / (blockBreakTime / animationCount) - 1);
            if (stage != targetLastStage) {
                sendBlockBreakAnimation(targetBlockPosition, stage);
            }
            this.targetLastStage = stage;
            if (stage > 9) {
                instance.breakBlock(this, targetBlockPosition);
                resetTargetBlock();
            }
        }

        // Experience orb pickup
        Chunk chunk = instance.getChunkAt(getPosition()); // TODO check surrounding chunks
        Set<Entity> entities = instance.getChunkEntities(chunk);
        BoundingBox livingBoundingBox = getBoundingBox().expand(1, 0.5f, 1);
        for (Entity entity : entities) {
            if (entity instanceof ExperienceOrb) {
                ExperienceOrb experienceOrb = (ExperienceOrb) entity;
                BoundingBox itemBoundingBox = experienceOrb.getBoundingBox();
                if (livingBoundingBox.intersect(itemBoundingBox)) {
                    synchronized (experienceOrb) {
                        if (experienceOrb.shouldRemove() || experienceOrb.isRemoveScheduled())
                            continue;
                        PickupExperienceEvent pickupExperienceEvent = new PickupExperienceEvent(experienceOrb.getExperienceCount());
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
            if (System.currentTimeMillis() - startEatingTime >= eatingTime) {
                refreshEating(false);

                triggerStatus((byte) 9); // Mark item use as finished
                ItemUpdateStateEvent itemUpdateStateEvent = callItemUpdateStateEvent(true);

                // Refresh hand
                boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
                refreshActiveHand(false, isOffHand, false);

                ItemStack foodItem = itemUpdateStateEvent.getItemStack();
                boolean isFood = foodItem.getMaterial().isFood();

                if (isFood) {
                    PlayerEatEvent playerEatEvent = new PlayerEatEvent(foodItem);
                    callEvent(PlayerEatEvent.class, playerEatEvent);
                }
            }
        }

        // Tick event
        callEvent(PlayerTickEvent.class, new PlayerTickEvent());


        // Multiplayer sync
        if (!getViewers().isEmpty()) {
            Position position = getPosition();
            boolean positionChanged = position.getX() != lastX || position.getZ() != lastZ || position.getY() != lastY;
            boolean viewChanged = position.getYaw() != lastYaw || position.getPitch() != lastPitch;
            ServerPacket updatePacket = null;
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
            } else if (viewChanged) {
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

            if (updatePacket != null) {
                if (optionalUpdatePacket != null) {
                    sendPacketsToViewers(updatePacket, optionalUpdatePacket);
                } else {
                    sendPacketToViewers(updatePacket);
                }
            }
        }

    }

    @Override
    public void kill() {
        if (!isDead()) {
            // send death message to player
            Component deathMessage;
            if (lastDamageSource != null) {
                deathMessage = lastDamageSource.buildDeathScreenMessage(this);
            } else { // may happen if killed by the server without applying damage
                deathMessage = TextComponent.of("Killed by poor programming.");
            }
            CombatEventPacket deathPacket = CombatEventPacket.death(this, Optional.empty(), deathMessage);
            playerConnection.sendPacket(deathPacket);

            // send death message to chat
            Component chatMessage;
            if (lastDamageSource != null) {
                chatMessage = lastDamageSource.buildChatMessage(this);
            } else { // may happen if killed by the server without applying damage
                chatMessage = TextComponent.of(getUsername() + " was killed by poor programming.");
            }
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
                player.sendMessage(chatMessage);
            });
        }
        super.kill();
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
        super.remove();
        clearBossBars();
        if (getOpenInventory() != null)
            getOpenInventory().removeViewer(this);
        this.viewableEntities.forEach(entity -> entity.removeViewer(this));
        this.viewableChunks.forEach(chunk -> chunk.removeViewer(this));
        resetTargetBlock();
        callEvent(PlayerDisconnectEvent.class, new PlayerDisconnectEvent());
    }

    @Override
    public boolean addViewer(Player player) {
        if (player == this)
            return false;

        boolean result = super.addViewer(player);
        PlayerConnection viewerConnection = player.getPlayerConnection();
        String property = "eyJ0aW1lc3RhbXAiOjE1NjU0ODMwODQwOTYsInByb2ZpbGVJZCI6ImFiNzBlY2I0MjM0NjRjMTRhNTJkN2EwOTE1MDdjMjRlIiwicHJvZmlsZU5hbWUiOiJUaGVNb2RlOTExIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkOTE2NzJiNTE0MmJhN2Y3MjA2ZTRjN2IwOTBkNzhlM2Y1ZDc2NDdiNWFmZDIyNjFhZDk4OGM0MWI2ZjcwYTEifX19";
        SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
        spawnPlayerPacket.entityId = getEntityId();
        spawnPlayerPacket.playerUuid = getUuid();
        spawnPlayerPacket.position = getPosition();

        PlayerInfoPacket pInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);
        PlayerInfoPacket.AddPlayer addP = new PlayerInfoPacket.AddPlayer(getUuid(), getUsername(), getGameMode(), 10);
        PlayerInfoPacket.AddPlayer.Property p = new PlayerInfoPacket.AddPlayer.Property("textures", property);//new PlayerInfoPacket.AddPlayer.Property("textures", properties.get(onlinePlayer.getUsername()));
        addP.properties.add(p);
        pInfoPacket.playerInfos.add(addP);

        viewerConnection.sendPacket(pInfoPacket);
        viewerConnection.sendPacket(spawnPlayerPacket);
        viewerConnection.sendPacket(getVelocityPacket());
        viewerConnection.sendPacket(getMetadataPacket());

        if (hasPassenger()) {
            viewerConnection.sendPacket(getPassengersPacket());
        }

        // Team
        if (team != null)
            viewerConnection.sendPacket(team.getTeamsCreationPacket());
        return result;
    }

    @Override
    public boolean removeViewer(Player player) {
        if (player == this)
            return false;

        boolean result = super.removeViewer(player);
        PlayerConnection viewerConnection = player.getPlayerConnection();
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.RemovePlayer(getUuid()));
        viewerConnection.sendPacket(playerInfoPacket);

        // Team
        if (team != null && team.getPlayers().size() == 1) // If team only contains "this" player
            viewerConnection.sendPacket(team.createTeamDestructionPacket());
        return result;
    }

    @Override
    public void setInstance(Instance instance) {
        Check.notNull(instance, "instance cannot be null!");
        Check.argCondition(this.instance == instance, "Instance should be different than the current one");

        boolean firstSpawn = this.instance == null; // TODO: Handle player reconnections, must be false in that case too
        for (Chunk viewableChunk : viewableChunks) {
            viewableChunk.removeViewer(this);
        }
        viewableChunks.clear();

        if (this.instance != null) {
            Dimension instanceDimension = instance.getDimension();
            if (dimension != instanceDimension)
                sendDimension(instanceDimension);
        }

        long[] visibleChunks = ChunkUtils.getChunksInRange(position, getChunkRange());
        int length = visibleChunks.length;

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < length; i++) {
            int[] chunkPos = ChunkUtils.getChunkCoord(visibleChunks[i]);
            int chunkX = chunkPos[0];
            int chunkZ = chunkPos[1];
            Consumer<Chunk> callback = (chunk) -> {
                if (chunk != null) {
                    viewableChunks.add(chunk);
                    chunk.addViewer(this);
                    instance.sendChunk(this, chunk);
                    updateViewPosition(chunk);
                }
                boolean isLast = counter.get() == length - 1;
                if (isLast) {
                    // This is the last chunk to be loaded , spawn player
                    super.setInstance(instance);
                    PlayerSpawnEvent spawnEvent = new PlayerSpawnEvent(instance, firstSpawn);
                    callEvent(PlayerSpawnEvent.class, spawnEvent);
                } else {
                    // Increment the counter of current loaded chunks
                    counter.incrementAndGet();
                }
            };

            // WARNING: if auto load is disabled and no chunks are loaded beforehand, player will be stuck.
            instance.loadOptionalChunk(chunkX, chunkZ, callback);
        }
    }

    public void sendBlockBreakAnimation(BlockPosition blockPosition, byte destroyStage) {
        BlockBreakAnimationPacket breakAnimationPacket = new BlockBreakAnimationPacket();
        breakAnimationPacket.entityId = getEntityId() + 1;
        breakAnimationPacket.blockPosition = blockPosition;
        breakAnimationPacket.destroyStage = destroyStage;
        sendPacketToViewersAndSelf(breakAnimationPacket);
    }

    // Use legacy color formatting
    public void sendMessage(String message) {
        sendMessage(Chat.fromLegacyText(message));
    }

    public void sendMessage(String message, char colorChar) {
        sendMessage(Chat.fromLegacyText(message, colorChar));
    }

    public void sendMessage(Component textObject) {
        String json = Chat.toJsonString(textObject);
        playerConnection.sendPacket(new ChatMessagePacket(json, ChatMessagePacket.Position.CHAT));
    }

    public void playSound(Sound sound, SoundCategory soundCategory, int x, int y, int z, float volume, float pitch) {
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
     * Plays a given effect at the given position for this player
     *
     * @param effect                the effect to play
     * @param x                     x position of the effect
     * @param y                     y position of the effect
     * @param z                     z position of the effect
     * @param data                  data for the effect
     * @param disableRelativeVolume disable volume scaling based on distance
     */
    public void playEffect(Effects effect, int x, int y, int z, int data, boolean disableRelativeVolume) {
        EffectPacket packet = new EffectPacket();
        packet.effectId = effect.getId();
        packet.position = new BlockPosition(x, y, z);
        packet.data = data;
        packet.disableRelativeVolume = disableRelativeVolume;
        playerConnection.sendPacket(packet);
    }

    public void stopSound() {
        StopSoundPacket stopSoundPacket = new StopSoundPacket();
        stopSoundPacket.flags = 0x00;
        playerConnection.sendPacket(stopSoundPacket);
    }

    public void sendHeaderFooter(Component header, Component footer) {
        PlayerListHeaderAndFooterPacket playerListHeaderAndFooterPacket = new PlayerListHeaderAndFooterPacket();
        playerListHeaderAndFooterPacket.emptyHeader = header == null;
        playerListHeaderAndFooterPacket.emptyFooter = footer == null;
        playerListHeaderAndFooterPacket.header = Chat.toJsonString(header);
        playerListHeaderAndFooterPacket.footer = Chat.toJsonString(footer);

        playerConnection.sendPacket(playerListHeaderAndFooterPacket);
    }

    public void sendHeaderFooter(String header, String footer, char colorChar) {
        sendHeaderFooter(Chat.fromLegacyText(header, colorChar), Chat.fromLegacyText(footer, colorChar));
    }

    private void sendTitle(Component title, TitlePacket.Action action) {
        TitlePacket titlePacket = new TitlePacket();
        titlePacket.action = action;

        switch (action) {
            case SET_TITLE:
                titlePacket.titleText = Chat.toJsonString(title);
                break;
            case SET_SUBTITLE:
                titlePacket.subtitleText = Chat.toJsonString(title);
                break;
            case SET_ACTION_BAR:
                titlePacket.actionBarText = Chat.toJsonString(title);
            default:
                throw new UnsupportedOperationException("Invalid TitlePacket.Action type!");
        }

        playerConnection.sendPacket(titlePacket);
    }

    public void sendTitleSubtitleMessage(Component title, Component subtitle) {
        sendTitle(title, TitlePacket.Action.SET_TITLE);
        sendTitle(subtitle, TitlePacket.Action.SET_SUBTITLE);
    }

    public void sendTitleMessage(Component title) {
        sendTitle(title, TitlePacket.Action.SET_TITLE);
    }

    public void sendTitleMessage(String title, char colorChar) {
        sendTitleMessage(Chat.fromLegacyText(title, colorChar));
    }

    public void sendTitleMessage(String title) {
        sendTitleMessage(title, Chat.COLOR_CHAR);
    }

    public void sendSubtitleMessage(Component subtitle) {
        sendTitle(subtitle, TitlePacket.Action.SET_SUBTITLE);
    }

    public void sendSubtitleMessage(String subtitle, char colorChar) {
        sendSubtitleMessage(Chat.fromLegacyText(subtitle, colorChar));
    }

    public void sendSubtitleMessage(String subtitle) {
        sendSubtitleMessage(subtitle, Chat.COLOR_CHAR);
    }

    public void sendActionBarMessage(Component actionBar) {
        sendTitle(actionBar, TitlePacket.Action.SET_ACTION_BAR);
    }

    public void sendActionBarMessage(String message, char colorChar) {
        sendActionBarMessage(Chat.fromLegacyText(message, colorChar));
    }

    public void sendActionBarMessage(String message) {
        sendActionBarMessage(message, Chat.COLOR_CHAR);
    }

    @Override
    public boolean isImmune(DamageType type) {
        if (!getGameMode().canTakeDamage()) {
            return type != DamageType.VOID;
        }
        return super.isImmune(type);
    }

    @Override
    public void setAttribute(Attribute attribute, float value) {
        super.setAttribute(attribute, value);
        if (playerConnection != null)
            playerConnection.sendPacket(getPropertiesPacket());
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        sendUpdateHealthPacket();
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
        sendUpdateHealthPacket();
    }

    public float getFoodSaturation() {
        return foodSaturation;
    }

    public void setFoodSaturation(float foodSaturation) {
        this.foodSaturation = foodSaturation;
        sendUpdateHealthPacket();
    }

    /**
     * @return true if the player is currently eating, false otherwise
     */
    public boolean isEating() {
        return isEating;
    }

    /**
     * @return the default eating time for the player
     */
    public long getDefaultEatingTime() {
        return defaultEatingTime;
    }

    /**
     * Used to change the default eating time animation
     *
     * @param defaultEatingTime the default eating time in milliseconds
     */
    public void setDefaultEatingTime(long defaultEatingTime) {
        this.defaultEatingTime = defaultEatingTime;
    }

    /**
     * @param item the item to drop
     * @return true if player can drop the item (event not cancelled), false otherwise
     */
    public boolean dropItem(ItemStack item) {
        ItemDropEvent itemDropEvent = new ItemDropEvent(item);
        callEvent(ItemDropEvent.class, itemDropEvent);
        return !itemDropEvent.isCancelled();
    }

    /**
     * Used to retrieve the default spawn point
     * can be altered by the {@link PlayerRespawnEvent#setRespawnPosition(Position)}
     *
     * @return the default respawn point
     */
    public Position getRespawnPoint() {
        return respawnPoint;
    }

    /**
     * Change the default spawn point
     *
     * @param respawnPoint
     */
    public void setRespawnPoint(Position respawnPoint) {
        this.respawnPoint = respawnPoint;
    }

    public void respawn() {
        if (!isDead())
            return;

        setFireForDuration(0);
        setOnFire(false);
        refreshHealth();
        RespawnPacket respawnPacket = new RespawnPacket();
        respawnPacket.dimension = getDimension();
        respawnPacket.gameMode = getGameMode();
        respawnPacket.levelType = getLevelType();
        getPlayerConnection().sendPacket(respawnPacket);
        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(getRespawnPoint());
        callEvent(PlayerRespawnEvent.class, respawnEvent);
        refreshIsDead(false);

        // Runnable called when teleportation is successful (after loading and sending necessary chunk)
        teleport(respawnEvent.getRespawnPosition(), this::refreshAfterTeleport);
    }

    public void refreshAfterTeleport() {
        getInventory().update();

        SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
        spawnPlayerPacket.entityId = getEntityId();
        spawnPlayerPacket.playerUuid = getUuid();
        spawnPlayerPacket.position = getPosition();
        sendPacketToViewers(spawnPlayerPacket);
        playerConnection.sendPacket(getPropertiesPacket());
        sendUpdateHealthPacket();
        syncEquipments();
    }

    protected void refreshHealth() {
        heal();
        this.food = 20;
        this.foodSaturation = 5;
    }

    protected void sendUpdateHealthPacket() {
        UpdateHealthPacket updateHealthPacket = new UpdateHealthPacket();
        updateHealthPacket.health = getHealth();
        updateHealthPacket.food = food;
        updateHealthPacket.foodSaturation = foodSaturation;
        playerConnection.sendPacket(updateHealthPacket);
    }

    /**
     * Used to change the percentage experience bar
     * This cannot change the displayed level, see {@link #setLevel(int)}
     *
     * @param exp a percentage between 0 and 1
     */
    public void setExp(float exp) {
        Check.argCondition(!MathUtils.isBetween(exp, 0, 1), "Exp should be between 0 and 1");

        this.exp = exp;
        sendExperienceUpdatePacket();
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

    protected void sendExperienceUpdatePacket() {
        SetExperiencePacket setExperiencePacket = new SetExperiencePacket();
        setExperiencePacket.percentage = exp;
        setExperiencePacket.level = level;
        playerConnection.sendPacket(setExperiencePacket);
    }

    protected void onChunkChange(Chunk lastChunk, Chunk newChunk) {
        long[] lastVisibleChunks = ChunkUtils.getChunksInRange(new Position(16 * lastChunk.getChunkX(), 0, 16 * lastChunk.getChunkZ()), MinecraftServer.CHUNK_VIEW_DISTANCE);
        long[] updatedVisibleChunks = ChunkUtils.getChunksInRange(new Position(16 * newChunk.getChunkX(), 0, 16 * newChunk.getChunkZ()), MinecraftServer.CHUNK_VIEW_DISTANCE);
        int[] oldChunks = ArrayUtils.getDifferencesBetweenArray(lastVisibleChunks, updatedVisibleChunks);
        int[] newChunks = ArrayUtils.getDifferencesBetweenArray(updatedVisibleChunks, lastVisibleChunks);

        // Unload old chunks
        for (int index : oldChunks) {
            int[] chunkPos = ChunkUtils.getChunkCoord(lastVisibleChunks[index]);
            UnloadChunkPacket unloadChunkPacket = new UnloadChunkPacket();
            unloadChunkPacket.chunkX = chunkPos[0];
            unloadChunkPacket.chunkZ = chunkPos[1];
            playerConnection.sendPacket(unloadChunkPacket);

            Chunk chunk = instance.getChunk(chunkPos[0], chunkPos[1]);
            if (chunk != null)
                chunk.removeViewer(this);
        }

        updateViewPosition(newChunk);

        // Load new chunks
        for (int i = 0; i < newChunks.length; i++) {
            int index = newChunks[i];
            int[] chunkPos = ChunkUtils.getChunkCoord(updatedVisibleChunks[index]);
            instance.loadOptionalChunk(chunkPos[0], chunkPos[1], chunk -> {
                if (chunk == null) {
                    // Cannot load chunk (auto load is not enabled)
                    return;
                }
                this.viewableChunks.add(chunk);
                chunk.addViewer(this);
                instance.sendChunk(this, chunk);
            });
        }
    }

    @Override
    public void teleport(Position position, Runnable callback) {
        super.teleport(position, () -> {
            updatePlayerPosition();
            if (callback != null)
                callback.run();
        });
    }

    @Override
    public void teleport(Position position) {
        teleport(position, null);
    }

    public String getUsername() {
        return username;
    }

    public PlayerConnection getPlayerConnection() {
        return playerConnection;
    }

    /**
     * @return true if the player is online, false otherwise
     */
    public boolean isOnline() {
        return playerConnection.isOnline();
    }

    /**
     * @return the player settings
     */
    public PlayerSettings getSettings() {
        return settings;
    }

    public PlayerInventory getInventory() {
        return inventory;
    }

    /**
     * Used to get the player latency,
     * computed by seeing how long it takes the client to answer the {@link KeepAlivePacket} packet
     *
     * @return the player latency
     */
    public int getLatency() {
        return latency;
    }

    /**
     * @return the player current dimension
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * @return the player current gamemode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Returns true iff this player is in creative. Used for code readability
     *
     * @return true if the player is in creative mode, false otherwise
     */
    public boolean isCreative() {
        return gameMode == GameMode.CREATIVE;
    }

    /**
     * Change the dimension of the player
     * Mostly unsafe since it requires sending chunks after
     *
     * @param dimension the new player dimension
     */
    public void sendDimension(Dimension dimension) {
        Check.notNull(dimension, "Dimension cannot be null!");
        Check.argCondition(dimension.equals(getDimension()), "The dimension need to be different than the current one!");

        refreshDimension(dimension);
        RespawnPacket respawnPacket = new RespawnPacket();
        respawnPacket.dimension = dimension;
        respawnPacket.gameMode = gameMode;
        respawnPacket.levelType = levelType;
        playerConnection.sendPacket(respawnPacket);
    }

    public void kick(Component message) {
        DisconnectPacket disconnectPacket = new DisconnectPacket();
        disconnectPacket.message = Chat.toJsonString(message);
        playerConnection.sendPacket(disconnectPacket);
        playerConnection.getChannel().close();
    }

    public void kick(String message) {
        kick(Chat.fromLegacyText(message));
    }

    public LevelType getLevelType() {
        return levelType;
    }

    /**
     * Used to change the player gamemode
     *
     * @param gameMode the new player gamemode
     */
    public void setGameMode(GameMode gameMode) {
        Check.notNull(gameMode, "GameMode cannot be null");
        ChangeGameStatePacket changeGameStatePacket = new ChangeGameStatePacket();
        changeGameStatePacket.reason = ChangeGameStatePacket.Reason.CHANGE_GAMEMODE;
        changeGameStatePacket.value = gameMode.getId();
        playerConnection.sendPacket(changeGameStatePacket);
        refreshGameMode(gameMode);
    }

    /**
     * Change the current held slot for the player
     *
     * @param slot the slot that the player has to held
     * @throws IllegalArgumentException if {@code slot} is not between 0 and 8
     */
    public void setHeldItemSlot(short slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "Slot has to be between 0 and 8");

        HeldItemChangePacket heldItemChangePacket = new HeldItemChangePacket();
        heldItemChangePacket.slot = slot;
        playerConnection.sendPacket(heldItemChangePacket);
        refreshHeldSlot(slot);
    }

    /**
     * @return the current held slot for the player
     */
    public short getHeldSlot() {
        return heldSlot;
    }

    public void setTeam(Team team) {
        if (this.team == team)
            return;

        if (this.team != null) {
            this.team.removePlayer(this);
        }

        this.team = team;
        if (team != null) {
            team.addPlayer(this);
            sendPacketToViewers(team.getTeamsCreationPacket()); // FIXME: only if viewer hasn't already register this team
        }
    }

    public void setBelowNameScoreboard(BelowNameScoreboard belowNameScoreboard) {
        if (this.belowNameScoreboard == belowNameScoreboard)
            return;

        if (this.belowNameScoreboard != null) {
            this.belowNameScoreboard.removeViewer(this);
        }

        this.belowNameScoreboard = belowNameScoreboard;
        if (belowNameScoreboard != null) {
            belowNameScoreboard.addViewer(this);
            belowNameScoreboard.displayScoreboard(this);
            getViewers().forEach(player -> belowNameScoreboard.addViewer(player));
        }
    }

    /**
     * @return the currently open inventory, null if there is not (player inventory is not detected)
     */
    public Inventory getOpenInventory() {
        return openInventory;
    }

    /**
     * Used to get the {@link CustomBlock} that the player is currently mining
     *
     * @return the currently mined {@link CustomBlock} by the player, null if there is not
     */
    public CustomBlock getCustomBlockTarget() {
        return targetCustomBlock;
    }

    /**
     * @return an unmodifiable {@link Set} containing all the current player viewable boss bars
     */
    public Set<BossBar> getBossBars() {
        return Collections.unmodifiableSet(bossBars);
    }

    /**
     * Open the specified Inventory, close the previous inventory if existing
     *
     * @param inventory the inventory to open
     * @return true if the inventory has been opened/sent to the player, false otherwise (cancelled by event)
     */
    public boolean openInventory(Inventory inventory) {
        Check.notNull(inventory, "Inventory cannot be null, use Player#closeInventory() to close current");

        InventoryOpenEvent inventoryOpenEvent = new InventoryOpenEvent(this, inventory);

        callCancellableEvent(InventoryOpenEvent.class, inventoryOpenEvent, () -> {

            if (getOpenInventory() != null) {
                closeInventory();
            }

            Inventory newInventory = inventoryOpenEvent.getInventory();

            OpenWindowPacket openWindowPacket = new OpenWindowPacket();
            openWindowPacket.windowId = newInventory.getWindowId();
            openWindowPacket.windowType = newInventory.getInventoryType().getWindowType();
            openWindowPacket.title = newInventory.getTitle();
            playerConnection.sendPacket(openWindowPacket);
            newInventory.addViewer(this);
            refreshOpenInventory(newInventory);

        });

        return !inventoryOpenEvent.isCancelled();
    }

    /**
     * Close the current inventory if there is any
     * It closes the player inventory if {@link #getOpenInventory()} returns null
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
            refreshOpenInventory(null);
        }
        playerConnection.sendPacket(closeWindowPacket);
        inventory.update();
    }

    /**
     * @return an unmodifiable {@link Set} containing all the chunks that the player sees
     */
    public Set<Chunk> getViewableChunks() {
        return Collections.unmodifiableSet(viewableChunks);
    }

    /**
     * Remove all the boss bars that the player has
     */
    public void clearBossBars() {
        this.bossBars.forEach(bossBar -> bossBar.removeViewer(this));
    }

    public void updateViewPosition(Chunk chunk) {
        UpdateViewPositionPacket updateViewPositionPacket = new UpdateViewPositionPacket(chunk);
        playerConnection.sendPacket(updateViewPositionPacket);
    }

    /**
     * Used for synchronization purpose, mainly for teleportation
     */
    protected void updatePlayerPosition() {
        PlayerPositionAndLookPacket positionAndLookPacket = new PlayerPositionAndLookPacket();
        positionAndLookPacket.position = position;
        positionAndLookPacket.flags = 0x00;
        positionAndLookPacket.teleportId = teleportId++;
        playerConnection.sendPacket(positionAndLookPacket);
    }

    /**
     * @return the player permission level
     */
    public int getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * Change the player permission level
     *
     * @param permissionLevel the new player permission level
     */
    public void setPermissionLevel(int permissionLevel) {
        Check.argCondition(!MathUtils.isBetween(permissionLevel, 0, 4), "permissionLevel has to be between 0 and 4");

        this.permissionLevel = permissionLevel;

        // Magic values: https://wiki.vg/Entity_statuses#Player
        byte permissionLevelStatus = (byte) (24 + permissionLevel);
        triggerStatus(permissionLevelStatus);
    }

    public void setReducedDebugScreenInformation(boolean reduced) {
        this.reducedDebugScreenInformation = reduced;

        // Magic values: https://wiki.vg/Entity_statuses#Player
        byte debugScreenStatus = (byte) (reduced ? 22 : 23);
        triggerStatus(debugScreenStatus);
    }

    public boolean hasReducedDebugScreenInformation() {
        return reducedDebugScreenInformation;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
        refreshAbilities();
    }

    /**
     * @return true if the player if flying, false otherwise
     */
    public boolean isFlying() {
        return flying;
    }

    /**
     * @param flying should the player fly
     */
    public void setFlying(boolean flying) {
        this.flying = flying;
        refreshAbilities();
    }

    /**
     * @return true if the player if allowed to fly, false otherwise
     */
    public boolean isAllowFlying() {
        return allowFlying;
    }

    /**
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
     * Change the player ability "Creative Mode"
     * <a href="https://wiki.vg/Protocol#Player_Abilities_.28clientbound.29">see</a>
     * <p>
     * WARNING: this has nothing to do with {@link CustomBlock#getBreakDelay(Player, BlockPosition)}
     *
     * @param instantBreak
     */
    public void setInstantBreak(boolean instantBreak) {
        this.instantBreak = instantBreak;
        refreshAbilities();
    }

    public float getFlyingSpeed() {
        return flyingSpeed;
    }

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
     * This is the map used to send the statistic packet
     * It is possible to add/remove/change statistic value directly into it
     *
     * @return the modifiable statistic map
     */
    public Map<PlayerStatistic, Integer> getStatisticValueMap() {
        return statisticValueMap;
    }

    public PlayerVehicleInformation getVehicleInformation() {
        return vehicleInformation;
    }

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
     * All packets in the queue are executed in the {@link #update()} method
     * It is used internally to add all received packet from the client
     * Could be used to "simulate" a received packet, but to use at your own risk
     *
     * @param packet the packet to add in the queue
     */
    public void addPacketToQueue(ClientPlayPacket packet) {
        this.packets.add(packet);
    }

    public void refreshLatency(int latency) {
        this.latency = latency;
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.UpdateLatency(getUuid(), latency));
        sendPacketToViewersAndSelf(playerInfoPacket);
    }

    public void refreshDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public void refreshGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void refreshLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    public void refreshOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void refreshKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }

    public void refreshHeldSlot(short slot) {
        this.heldSlot = slot;
        syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);

        refreshEating(false);
    }

    public void refreshOpenInventory(Inventory openInventory) {
        this.openInventory = openInventory;
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

    public ItemUpdateStateEvent callItemUpdateStateEvent(boolean allowFood) {
        Material mainHandMat = Material.fromId(getItemInMainHand().getMaterialId());
        Material offHandMat = Material.fromId(getItemInOffHand().getMaterialId());
        boolean isOffhand = offHandMat.hasState();

        ItemStack updatedItem = isOffhand ? getItemInOffHand() :
                mainHandMat.hasState() ? getItemInMainHand() : null;
        if (updatedItem == null) // No item with state, cancel
            return null;

        boolean isFood = updatedItem.getMaterial().isFood();

        if (isFood && !allowFood)
            return null;

        ItemUpdateStateEvent itemUpdateStateEvent = new ItemUpdateStateEvent(updatedItem,
                isOffhand ? Hand.OFF : Hand.MAIN);
        callEvent(ItemUpdateStateEvent.class, itemUpdateStateEvent);

        return itemUpdateStateEvent;
    }

    public void refreshTargetBlock(CustomBlock targetCustomBlock, BlockPosition targetBlockPosition, int breakTime) {
        this.targetCustomBlock = targetCustomBlock;
        this.targetBlockPosition = targetBlockPosition;
        this.targetBlockTime = targetBlockPosition == null ? 0 : System.currentTimeMillis();
        this.blockBreakTime = breakTime;
    }

    public void resetTargetBlock() {
        if (targetBlockPosition != null)
            sendBlockBreakAnimation(targetBlockPosition, (byte) -1); // Clear the break animation
        this.targetCustomBlock = null;
        this.targetBlockPosition = null;
        this.targetBlockTime = 0;
    }

    public void refreshAddBossbar(BossBar bossBar) {
        this.bossBars.add(bossBar);
    }

    public void refreshRemoveBossbar(BossBar bossBar) {
        this.bossBars.remove(bossBar);
    }

    public void refreshVehicleSteer(float sideways, float forward, boolean jump, boolean unmount) {
        this.vehicleInformation.refresh(sideways, forward, jump, unmount);
    }

    public int getChunkRange() {
        int serverRange = MinecraftServer.CHUNK_VIEW_DISTANCE;
        int playerRange = getSettings().viewDistance;
        if (playerRange == 0) {
            return serverRange; // Didn't receive settings packet yet (is the case on login)
        } else {
            return Math.min(playerRange, serverRange);
        }
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    @Override
    public ItemStack getItemInMainHand() {
        return inventory.getItemInMainHand();
    }

    @Override
    public void setItemInMainHand(ItemStack itemStack) {
        inventory.setItemInMainHand(itemStack);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return inventory.getItemInOffHand();
    }

    @Override
    public void setItemInOffHand(ItemStack itemStack) {
        inventory.setItemInOffHand(itemStack);
    }

    @Override
    public ItemStack getHelmet() {
        return inventory.getHelmet();
    }

    @Override
    public void setHelmet(ItemStack itemStack) {
        inventory.setHelmet(itemStack);
    }

    @Override
    public ItemStack getChestplate() {
        return inventory.getChestplate();
    }

    @Override
    public void setChestplate(ItemStack itemStack) {
        inventory.setChestplate(itemStack);
    }

    @Override
    public ItemStack getLeggings() {
        return inventory.getLeggings();
    }

    @Override
    public void setLeggings(ItemStack itemStack) {
        inventory.setLeggings(itemStack);
    }

    @Override
    public ItemStack getBoots() {
        return inventory.getBoots();
    }

    @Override
    public void setBoots(ItemStack itemStack) {
        inventory.setBoots(itemStack);
    }

    public enum Hand {
        MAIN,
        OFF
    }

    // Settings enum

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

        public String getLocale() {
            return locale;
        }

        public byte getViewDistance() {
            return viewDistance;
        }

        public ChatMode getChatMode() {
            return chatMode;
        }

        public boolean hasChatColors() {
            return chatColors;
        }

        public byte getDisplayedSkinParts() {
            return displayedSkinParts;
        }

        public MainHand getMainHand() {
            return mainHand;
        }

        public void refresh(String locale, byte viewDistance, ChatMode chatMode, boolean chatColors, byte displayedSkinParts, MainHand mainHand) {
            this.locale = locale;
            this.viewDistance = viewDistance;
            this.chatMode = chatMode;
            this.chatColors = chatColors;
            this.displayedSkinParts = displayedSkinParts;
            this.mainHand = mainHand;
        }
    }

}
