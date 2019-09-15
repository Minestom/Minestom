package fr.themode.minestom.entity;

import club.thectm.minecraft.text.TextObject;
import com.google.gson.JsonObject;
import fr.themode.minestom.Main;
import fr.themode.minestom.bossbar.BossBar;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.collision.BoundingBox;
import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.property.Attribute;
import fr.themode.minestom.event.*;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.InstanceContainer;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.instance.demo.ChunkGeneratorDemo;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.play.*;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.*;
import fr.themode.minestom.world.Dimension;
import fr.themode.minestom.world.LevelType;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Player extends LivingEntity {

    private long lastKeepAlive;

    private String username;
    private PlayerConnection playerConnection;
    private ConcurrentLinkedQueue<ClientPlayPacket> packets = new ConcurrentLinkedQueue<>();

    private Dimension dimension;
    private GameMode gameMode;
    private LevelType levelType;

    protected boolean onGround;

    private static InstanceContainer instanceContainer;

    static {
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        //instanceContainer = Main.getInstanceManager().createInstanceContainer(new File("C:\\Users\\themo\\OneDrive\\Bureau\\Minestom data"));
        instanceContainer = Main.getInstanceManager().createInstanceContainer();
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(chunkGeneratorDemo);
        int loopStart = -2;
        int loopEnd = 2;
        long time = System.currentTimeMillis();
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                instanceContainer.loadChunk(x, z);
            }
        System.out.println("Time to load all chunks: " + (System.currentTimeMillis() - time) + " ms");
    }

    protected Set<Entity> viewableEntities = new CopyOnWriteArraySet<>();
    protected Set<Chunk> viewableChunks = new CopyOnWriteArraySet<>();

    private PlayerSettings settings;
    private float exp;
    private int level;
    private PlayerInventory inventory;
    private short heldSlot;
    private Inventory openInventory;

    private int food;
    private float foodSaturation;

    private CustomBlock targetCustomBlock;
    private BlockPosition targetBlockPosition;
    private long targetBlockTime;
    private byte targetLastStage;

    private Set<BossBar> bossBars = new CopyOnWriteArraySet<>();

    // Vehicle
    private float sideways;
    private float forward;
    private boolean jump;
    private boolean unmount;

    public Player(UUID uuid, String username, PlayerConnection playerConnection) {
        super(100);
        this.uuid = uuid;
        this.username = username;
        this.playerConnection = playerConnection;

        setBoundingBox(0.8f, 1.8f, 0.8f);

        playerConnection.sendPacket(getPropertiesPacket()); // Send default properties
        refreshHealth();

        this.settings = new PlayerSettings();
        this.inventory = new PlayerInventory(this);

        setCanPickupItem(true); // By default

        setEventCallback(AttackEvent.class, event -> {
            Entity entity = event.getTarget();
            if (entity instanceof EntityCreature) {
                ((EntityCreature) entity).damage(-1);
                Vector velocity = getPosition().clone().getDirection().multiply(6);
                velocity.setY(4f);
                entity.setVelocity(velocity, 150);
                sendMessage("You attacked an entity!");
            } else if (entity instanceof Player) {
                Player player = (Player) entity;
                Vector velocity = getPosition().clone().getDirection().multiply(4);
                velocity.setY(3.5f);
                player.setVelocity(velocity, 150);
                player.damage(2);
                sendMessage("ATTACK");
            }
        });

        setEventCallback(PlayerBlockPlaceEvent.class, event -> {
            if (event.getHand() != Hand.MAIN)
                return;

            for (Player player : instance.getPlayers()) {
                if (player != this)
                    player.teleport(getPosition());
            }
        });

        setEventCallback(PlayerStartDiggingEvent.class, event -> {
            BlockPosition blockPosition = event.getBlockPosition();
            Data data = getInstance().getBlockData(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            if (data == null) {
                sendMessage("DATA IS NULL");
                return;
            }
            sendMessage("BLOCK DATA: " + data.get("value"));
        });

        setEventCallback(PickupItemEvent.class, event -> {
            event.setCancelled(!getInventory().addItemStack(event.getItemStack())); // Cancel event if player does not have enough inventory space
        });

        setEventCallback(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(instanceContainer);
        });

        setEventCallback(PlayerSpawnEvent.class, event -> {
            setGameMode(GameMode.SURVIVAL);
            teleport(new Position(0, 66, 0));

            /*Random random = new Random();
            for (int i = 0; i < 50; i++) {
                ChickenCreature chickenCreature = new ChickenCreature();
                chickenCreature.refreshPosition(random.nextInt(100), 65, random.nextInt(100));
                chickenCreature.setInstance(getInstance());
            }*/
            //chickenCreature.addPassenger(this);

            /*for (int ix = 0; ix < 4; ix++)
                for (int iz = 0; iz < 4; iz++) {
                    ItemEntity itemEntity = new ItemEntity(new ItemStack(1, (byte) 32));
                    itemEntity.refreshPosition(ix, 68, iz);
                    //itemEntity.setNoGravity(true);
                    itemEntity.setInstance(getInstance());
                    //itemEntity.remove();
                }*/

            getInventory().addItemStack(new ItemStack(1, (byte) 75));
            //getInventory().addItemStack(new ItemStack(1, (byte) 100));

            TeamsPacket teamsPacket = new TeamsPacket();
            teamsPacket.teamName = "TEAMNAME" + new Random().nextInt(100);
            teamsPacket.action = TeamsPacket.Action.CREATE_TEAM;
            teamsPacket.teamDisplayName = "WOWdisplay";
            teamsPacket.nameTagVisibility = "always";
            teamsPacket.teamColor = 2;
            teamsPacket.teamPrefix = "pre";
            teamsPacket.teamSuffix = "suf";
            teamsPacket.collisionRule = "never";
            teamsPacket.entities = new String[]{getUsername()};
            sendPacketToViewersAndSelf(teamsPacket);

            setAttribute(Attribute.MAX_HEALTH, 10);
            heal();

            /*Scoreboard scoreboard = new Scoreboard("Scoreboard Title");
            for (int i = 0; i < 15; i++) {
                scoreboard.createLine(new Scoreboard.ScoreboardLine("id" + i, "Hey guys " + i, i));
            }
            scoreboard.addViewer(this);
            scoreboard.updateLineContent("id3", "I HAVE BEEN UPDATED &2TEST");*/
        });
    }

    @Override
    public void update() {

        // Flush all pending packets
        playerConnection.flush();

        // Process sent packets
        ClientPlayPacket packet;
        while ((packet = packets.poll()) != null) {
            packet.process(this);
        }

        super.update(); // Super update (item pickup)

        // Target block stage
        if (targetCustomBlock != null) {
            int timeBreak = targetCustomBlock.getBreakDelay(this);
            int animationCount = 10;
            long since = System.currentTimeMillis() - targetBlockTime;
            byte stage = (byte) (since / (timeBreak / animationCount));
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
                EntityLookAndRelativeMovePacket entityLookAndRelativeMovePacket = new EntityLookAndRelativeMovePacket();
                entityLookAndRelativeMovePacket.entityId = getEntityId();
                entityLookAndRelativeMovePacket.deltaX = (short) ((position.getX() * 32 - lastX * 32) * 128);
                entityLookAndRelativeMovePacket.deltaY = (short) ((position.getY() * 32 - lastY * 32) * 128);
                entityLookAndRelativeMovePacket.deltaZ = (short) ((position.getZ() * 32 - lastZ * 32) * 128);
                entityLookAndRelativeMovePacket.yaw = position.getYaw();
                entityLookAndRelativeMovePacket.pitch = position.getPitch();
                entityLookAndRelativeMovePacket.onGround = onGround;

                lastX = position.getX();
                lastY = position.getY();
                lastZ = position.getZ();
                lastYaw = position.getYaw();
                lastPitch = position.getPitch();
                updatePacket = entityLookAndRelativeMovePacket;
            } else if (positionChanged) {
                EntityRelativeMovePacket entityRelativeMovePacket = new EntityRelativeMovePacket();
                entityRelativeMovePacket.entityId = getEntityId();
                entityRelativeMovePacket.deltaX = (short) ((position.getX() * 32 - lastX * 32) * 128);
                entityRelativeMovePacket.deltaY = (short) ((position.getY() * 32 - lastY * 32) * 128);
                entityRelativeMovePacket.deltaZ = (short) ((position.getZ() * 32 - lastZ * 32) * 128);
                entityRelativeMovePacket.onGround = onGround;
                lastX = position.getX();
                lastY = position.getY();
                lastZ = position.getZ();
                updatePacket = entityRelativeMovePacket;
            } else if (viewChanged) {
                EntityLookPacket entityLookPacket = new EntityLookPacket();
                entityLookPacket.entityId = getEntityId();
                entityLookPacket.yaw = position.getYaw();
                entityLookPacket.pitch = position.getPitch();
                entityLookPacket.onGround = onGround;

                lastYaw = position.getYaw();
                lastPitch = position.getPitch();
                updatePacket = entityLookPacket;
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
    public void spawn() {

    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void remove() {
        clearBossBars();
        if (getOpenInventory() != null)
            getOpenInventory().removeViewer(this);
        this.viewableEntities.forEach(entity -> entity.removeViewer(this));
        this.viewableChunks.forEach(chunk -> chunk.removeViewer(this));
        resetTargetBlock();
        callEvent(PlayerDisconnectEvent.class, new PlayerDisconnectEvent());
        super.remove();
    }

    @Override
    public void addViewer(Player player) {
        if (player == this)
            return;
        super.addViewer(player);
        PlayerConnection connection = player.getPlayerConnection();
        String property = "eyJ0aW1lc3RhbXAiOjE1NjU0ODMwODQwOTYsInByb2ZpbGVJZCI6ImFiNzBlY2I0MjM0NjRjMTRhNTJkN2EwOTE1MDdjMjRlIiwicHJvZmlsZU5hbWUiOiJUaGVNb2RlOTExIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkOTE2NzJiNTE0MmJhN2Y3MjA2ZTRjN2IwOTBkNzhlM2Y1ZDc2NDdiNWFmZDIyNjFhZDk4OGM0MWI2ZjcwYTEifX19";
        SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
        spawnPlayerPacket.entityId = getEntityId();
        spawnPlayerPacket.playerUuid = getUuid();
        spawnPlayerPacket.position = getPosition();

        PlayerInfoPacket pInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);
        PlayerInfoPacket.AddPlayer addP = new PlayerInfoPacket.AddPlayer(getUuid(), getUsername(), GameMode.CREATIVE, 10);
        PlayerInfoPacket.AddPlayer.Property p = new PlayerInfoPacket.AddPlayer.Property("textures", property);//new PlayerInfoPacket.AddPlayer.Property("textures", properties.get(onlinePlayer.getUsername()));
        addP.properties.add(p);
        pInfoPacket.playerInfos.add(addP);

        connection.sendPacket(pInfoPacket);
        connection.sendPacket(spawnPlayerPacket);
        connection.sendPacket(getMetadataPacket());

        for (EntityEquipmentPacket.Slot slot : EntityEquipmentPacket.Slot.values()) {
            player.playerConnection.sendPacket(getEquipmentPacket(slot));
        }
    }

    @Override
    public void removeViewer(Player player) {
        if (player == this)
            return;
        super.removeViewer(player);
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.RemovePlayer(getUuid()));
        player.playerConnection.sendPacket(playerInfoPacket);
    }

    @Override
    public void setInstance(Instance instance) {
        if (instance == null)
            throw new IllegalArgumentException("instance cannot be null!");
        if (this.instance == instance)
            throw new IllegalArgumentException("Instance should be different than the current one");

        for (Chunk viewableChunk : viewableChunks) {
            viewableChunk.removeViewer(this);
        }
        viewableChunks.clear();

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
                }
                boolean isLast = counter.get() == length - 1;
                if (isLast) {
                    // This is the last chunk to be loaded, spawn player
                    super.setInstance(instance);
                    PlayerSpawnEvent spawnEvent = new PlayerSpawnEvent(instance);
                    callEvent(PlayerSpawnEvent.class, spawnEvent);
                    updateViewPosition(chunk);
                } else {
                    // Increment the counter of current loaded chunks
                    counter.incrementAndGet();
                }
            };

            // WARNING: if auto load is disabled and no chunks are loaded beforehand, player will be stuck.
            instance.loadOptionalChunk(chunkX, chunkZ, callback);
        }
    }

    @Override
    public void kill() {
        this.isDead = true;
        refreshIsDead(true);
        EntityStatusPacket entityStatusPacket = new EntityStatusPacket();
        entityStatusPacket.entityId = getEntityId();
        entityStatusPacket.status = 3; // Death sound/animation
        sendPacketToViewers(entityStatusPacket);
        DeathEvent deathEvent = new DeathEvent();
        callEvent(DeathEvent.class, deathEvent);
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
        sendMessage(Chat.legacyText(message));
    }

    public void sendMessage(String message, char colorChar) {
        sendMessage(Chat.legacyText(message, colorChar));
    }

    public void sendMessage(JsonObject jsonObject) {
        ChatMessagePacket chatMessagePacket = new ChatMessagePacket(jsonObject.toString(), ChatMessagePacket.Position.CHAT);
        playerConnection.sendPacket(chatMessagePacket);
    }

    public void sendMessage(TextObject textObject) {
        sendMessage(textObject.toJson());
    }

    @Override
    public void damage(float value) {
        if (getGameMode() == GameMode.CREATIVE)
            return;

        super.damage(value);
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

    public void respawn() {
        if (!isDead())
            return;

        refreshHealth();
        RespawnPacket respawnPacket = new RespawnPacket();
        respawnPacket.dimension = getDimension();
        respawnPacket.gameMode = getGameMode();
        respawnPacket.levelType = getLevelType();
        getPlayerConnection().sendPacket(respawnPacket);
        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(getPosition());
        callEvent(PlayerRespawnEvent.class, respawnEvent);
        refreshIsDead(false);

        // Runnable called when teleportation is successfull (after loading and sending necessary chunk)
        teleport(respawnEvent.getRespawnPosition(), () -> {
            getInventory().update();

            SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
            spawnPlayerPacket.entityId = getEntityId();
            spawnPlayerPacket.playerUuid = getUuid();
            spawnPlayerPacket.position = getPosition();
            sendPacketToViewers(spawnPlayerPacket);
            playerConnection.sendPacket(getPropertiesPacket());
            sendUpdateHealthPacket();
            syncEquipments();

        });
    }

    private void refreshHealth() {
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

    public void setExp(float exp) {
        if (exp < 0 || exp > 1)
            throw new IllegalArgumentException("Exp should be between 0 and 1");
        this.exp = exp;
        sendExperienceUpdatePacket();
    }

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
        float dx = newChunk.getChunkX() - lastChunk.getChunkX();
        float dz = newChunk.getChunkZ() - lastChunk.getChunkZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        boolean isFar = distance >= Main.CHUNK_VIEW_DISTANCE / 2;

        long[] lastVisibleChunks = ChunkUtils.getChunksInRange(new Position(16 * lastChunk.getChunkX(), 0, 16 * lastChunk.getChunkZ()), Main.CHUNK_VIEW_DISTANCE);
        long[] updatedVisibleChunks = ChunkUtils.getChunksInRange(new Position(16 * newChunk.getChunkX(), 0, 16 * newChunk.getChunkZ()), Main.CHUNK_VIEW_DISTANCE);
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
            boolean isLast = i == newChunks.length - 1;
            int index = newChunks[i];
            int[] chunkPos = ChunkUtils.getChunkCoord(updatedVisibleChunks[index]);
            instance.loadOptionalChunk(chunkPos[0], chunkPos[1], chunk -> {
                if (chunk == null) {
                    return; // Cannot load chunk (auto load is not enabled)
                }
                this.viewableChunks.add(chunk);
                chunk.addViewer(this);
                instance.sendChunk(this, chunk);
                if (isFar && isLast) {
                    updatePlayerPosition();
                }
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

    public boolean isOnline() {
        return playerConnection.isOnline();
    }

    public PlayerSettings getSettings() {
        return settings;
    }

    public PlayerInventory getInventory() {
        return inventory;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setDimension(Dimension dimension) {
        if (dimension == null)
            throw new IllegalArgumentException("Dimension cannot be null!");
        if (dimension.equals(getDimension()))
            return;
        RespawnPacket respawnPacket = new RespawnPacket();
        respawnPacket.dimension = dimension;
        respawnPacket.gameMode = gameMode;
        respawnPacket.levelType = levelType;
        playerConnection.sendPacket(respawnPacket);
    }

    public void kick(String message) {
        DisconnectPacket disconnectPacket = new DisconnectPacket();
        disconnectPacket.message = message;
        playerConnection.sendPacket(disconnectPacket);
        playerConnection.getClient().close();
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public void setGameMode(GameMode gameMode) {
        ChangeGameStatePacket changeGameStatePacket = new ChangeGameStatePacket();
        changeGameStatePacket.reason = ChangeGameStatePacket.Reason.CHANGE_GAMEMODE;
        changeGameStatePacket.value = gameMode.getId();
        playerConnection.sendPacket(changeGameStatePacket);
        refreshGameMode(gameMode);
    }

    public void setHeldItemSlot(short slot) {
        if (slot < 0 || slot > 8)
            throw new IllegalArgumentException("Slot has to be between 0 and 8");
        HeldItemChangePacket heldItemChangePacket = new HeldItemChangePacket();
        heldItemChangePacket.slot = slot;
        playerConnection.sendPacket(heldItemChangePacket);
        refreshHeldSlot(slot);
    }

    public short getHeldSlot() {
        return heldSlot;
    }

    public Inventory getOpenInventory() {
        return openInventory;
    }

    public CustomBlock getCustomBlockTarget() {
        return targetCustomBlock;
    }

    public Set<BossBar> getBossBars() {
        return Collections.unmodifiableSet(bossBars);
    }

    public float getVehicleSideways() {
        return sideways;
    }

    public float getVehicleForward() {
        return forward;
    }

    public boolean isVehicleJump() {
        return jump;
    }

    public boolean isVehicleUnmount() {
        return unmount;
    }

    public void openInventory(Inventory inventory) {
        if (inventory == null)
            throw new IllegalArgumentException("Inventory cannot be null, use Player#closeInventory() to close current");

        if (getOpenInventory() != null) {
            getOpenInventory().removeViewer(this);
        }

        OpenWindowPacket openWindowPacket = new OpenWindowPacket();
        openWindowPacket.windowId = inventory.getWindowId();
        openWindowPacket.windowType = inventory.getInventoryType().getWindowType();
        openWindowPacket.title = inventory.getTitle();
        playerConnection.sendPacket(openWindowPacket);
        inventory.addViewer(this);
        refreshOpenInventory(inventory);
    }

    public void closeInventory() {
        Inventory openInventory = getOpenInventory();
        CloseWindowPacket closeWindowPacket = new CloseWindowPacket();
        if (openInventory == null) {
            closeWindowPacket.windowId = 0;
        } else {
            closeWindowPacket.windowId = openInventory.getWindowId();
            openInventory.removeViewer(this);
            refreshOpenInventory(null);
        }
        playerConnection.sendPacket(closeWindowPacket);
        inventory.update();
    }

    public Set<Chunk> getViewableChunks() {
        return Collections.unmodifiableSet(viewableChunks);
    }

    public void clearBossBars() {
        this.bossBars.forEach(bossBar -> bossBar.removeViewer(this));
    }

    public void syncEquipment(EntityEquipmentPacket.Slot slot) {
        sendPacketToViewers(getEquipmentPacket(slot));
    }

    public void syncEquipments() {
        for (EntityEquipmentPacket.Slot slot : EntityEquipmentPacket.Slot.values()) {
            syncEquipment(slot);
        }
    }

    protected EntityEquipmentPacket getEquipmentPacket(EntityEquipmentPacket.Slot slot) {
        EntityEquipmentPacket equipmentPacket = new EntityEquipmentPacket();
        equipmentPacket.entityId = getEntityId();
        equipmentPacket.slot = slot;
        equipmentPacket.itemStack = inventory.getEquipment(slot);
        return equipmentPacket;
    }

    public void updateViewPosition(Chunk chunk) {
        UpdateViewPositionPacket updateViewPositionPacket = new UpdateViewPositionPacket(chunk);
        playerConnection.sendPacket(updateViewPositionPacket);
    }

    protected void updatePlayerPosition() {
        PlayerPositionAndLookPacket positionAndLookPacket = new PlayerPositionAndLookPacket();
        positionAndLookPacket.position = position;
        positionAndLookPacket.flags = 0x00;
        positionAndLookPacket.teleportId = 67;
        playerConnection.sendPacket(positionAndLookPacket);
    }

    public void addPacketToQueue(ClientPlayPacket packet) {
        this.packets.add(packet);
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
    }

    public void refreshOpenInventory(Inventory openInventory) {
        this.openInventory = openInventory;
    }

    public void refreshTargetBlock(CustomBlock targetCustomBlock, BlockPosition targetBlockPosition) {
        this.targetCustomBlock = targetCustomBlock;
        this.targetBlockPosition = targetBlockPosition;
        this.targetBlockTime = targetBlockPosition == null ? 0 : System.currentTimeMillis();
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
        this.sideways = sideways;
        this.forward = forward;
        this.jump = jump;
        this.unmount = unmount;
    }

    public int getChunkRange() {
        int serverRange = Main.CHUNK_VIEW_DISTANCE;
        int playerRange = getSettings().viewDistance;
        if (playerRange == 0) {
            return serverRange; // Didn't receive settings packet yet
        } else {
            return playerRange < serverRange ? playerRange : serverRange;
        }
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public enum Hand {
        MAIN,
        OFF
    }

    public enum MainHand {
        LEFT,
        RIGHT;
    }

    public enum ChatMode {
        ENABLED,
        COMMANDS_ONLY,
        HIDDEN;
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
