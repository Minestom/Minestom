package fr.themode.minestom.entity;

import fr.themode.minestom.bossbar.BossBar;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.event.PickupItemEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.CustomBlock;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.play.*;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Position;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class Player extends LivingEntity {

    private long lastKeepAlive;

    private String username;
    private PlayerConnection playerConnection;
    private LinkedList<ClientPlayPacket> packets = new LinkedList<>();

    private GameMode gameMode;
    private PlayerInventory inventory;
    private short heldSlot;
    private Inventory openInventory;

    private CustomBlock targetCustomBlock;
    private Position targetBlockPosition;
    private long targetBlockTime;

    private Set<BossBar> bossBars = new CopyOnWriteArraySet<>();

    // Synchronization
    private long synchronizationDelay = 1000; // In ms
    private long lastSynchronizationTime;

    // Vehicle
    private float sideways;
    private float forward;

    public Player(UUID uuid, String username, PlayerConnection playerConnection) {
        super(93); // FIXME verify
        this.uuid = uuid;
        this.username = username;
        this.playerConnection = playerConnection;

        this.inventory = new PlayerInventory(this);

        /*setEventCallback(PickupItemEvent.class, event -> {
            sendMessage("Hey you're trying to pick an item!");
            event.setCancelled(true);
        });*/

        /*setEventCallback(StartDiggingEvent.class, event -> {
            Random random = new Random();
            boolean cancel = random.nextBoolean();
            event.setCancelled(cancel);
            sendMessage("Cancelled: " + cancel);
        });*/

        /*setEventCallback(BlockPlaceEvent.class, event -> {
            event.setCancelled(true);
            sendMessage("CANCELLED");
        });*/
    }

    @Override
    public void update() {
        synchronized (packets) {
            while (!packets.isEmpty()) {
                ClientPlayPacket packet = packets.pollFirst();
                packet.process(this);
            }
        }

        // Target block stage
        if (instance != null && targetCustomBlock != null) {
            int timeBreak = targetCustomBlock.getBreakDelay(this);
            int animationCount = 10;
            long since = System.currentTimeMillis() - targetBlockTime;
            byte stage = (byte) (since / (timeBreak / animationCount));
            sendBlockBreakAnimation(targetBlockPosition, stage);// TODO send to all near players
            if (stage > 9) {
                instance.breakBlock(this, targetBlockPosition, targetCustomBlock);
                resetTargetBlock();
            }
        }

        // Item pickup
        if (instance != null) {
            Chunk chunk = instance.getChunkAt(getX(), getZ()); // TODO check surrounding chunks
            Set<ObjectEntity> objectEntities = chunk.getObjectEntities();
            for (ObjectEntity objectEntity : objectEntities) {
                if (objectEntity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) objectEntity;
                    if (!itemEntity.isPickable())
                        continue;
                    float distance = getDistance(objectEntity);
                    if (distance <= 2.04) {
                        synchronized (itemEntity) {
                            if (itemEntity.shouldRemove())
                                continue;
                            ItemStack item = itemEntity.getItemStack();
                            PickupItemEvent pickupItemEvent = new PickupItemEvent(item);
                            callCancellableEvent(PickupItemEvent.class, pickupItemEvent, () -> {
                                CollectItemPacket collectItemPacket = new CollectItemPacket();
                                collectItemPacket.collectedEntityId = itemEntity.getEntityId();
                                collectItemPacket.collectorEntityId = getEntityId();
                                collectItemPacket.pickupItemCount = item.getAmount();
                                instance.getPlayers().forEach(player -> player.getPlayerConnection().sendPacket(collectItemPacket));
                                getInventory().addItemStack(item);
                                objectEntity.remove();
                            });
                        }
                    }
                }
            }
        }


        // Multiplayer sync
        boolean positionChanged = x != lastX || z != lastZ || y != lastY;
        boolean viewChanged = yaw != lastYaw || pitch != lastPitch;
        ServerPacket updatePacket = null;
        ServerPacket optionalUpdatePacket = null;
        if (positionChanged && viewChanged) {
            EntityLookAndRelativeMovePacket entityLookAndRelativeMovePacket = new EntityLookAndRelativeMovePacket();
            entityLookAndRelativeMovePacket.entityId = getEntityId();
            entityLookAndRelativeMovePacket.deltaX = (short) ((x * 32 - lastX * 32) * 128);
            entityLookAndRelativeMovePacket.deltaY = (short) ((y * 32 - lastY * 32) * 128);
            entityLookAndRelativeMovePacket.deltaZ = (short) ((z * 32 - lastZ * 32) * 128);
            entityLookAndRelativeMovePacket.yaw = yaw;
            entityLookAndRelativeMovePacket.pitch = pitch;
            entityLookAndRelativeMovePacket.onGround = onGround;

            EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = yaw;

            lastX = x;
            lastY = y;
            lastZ = z;
            lastYaw = yaw;
            lastPitch = pitch;
            updatePacket = entityLookAndRelativeMovePacket;
            optionalUpdatePacket = entityHeadLookPacket;
        } else if (positionChanged) {
            EntityRelativeMovePacket entityRelativeMovePacket = new EntityRelativeMovePacket();
            entityRelativeMovePacket.entityId = getEntityId();
            entityRelativeMovePacket.deltaX = (short) ((x * 32 - lastX * 32) * 128);
            entityRelativeMovePacket.deltaY = (short) ((y * 32 - lastY * 32) * 128);
            entityRelativeMovePacket.deltaZ = (short) ((z * 32 - lastZ * 32) * 128);
            entityRelativeMovePacket.onGround = onGround;
            lastX = x;
            lastY = y;
            lastZ = z;
            updatePacket = entityRelativeMovePacket;
        } else if (viewChanged) {
            EntityLookPacket entityLookPacket = new EntityLookPacket();
            entityLookPacket.entityId = getEntityId();
            entityLookPacket.yaw = yaw;
            entityLookPacket.pitch = pitch;
            entityLookPacket.onGround = onGround;

            EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = yaw;

            lastYaw = yaw;
            lastPitch = pitch;
            updatePacket = entityLookPacket;
            optionalUpdatePacket = entityHeadLookPacket;
        }
        if (updatePacket != null) {
            if (optionalUpdatePacket != null) {
                sendPacketsToViewers(updatePacket, optionalUpdatePacket);
            } else {
                sendPacketToViewers(updatePacket);
            }
        }
        playerConnection.sendPacket(new UpdateViewPositionPacket(Math.floorDiv((int) x, 16), Math.floorDiv((int) z, 16)));

        // Synchronization
        long time = System.currentTimeMillis();
        if (time - lastSynchronizationTime >= synchronizationDelay) {
            lastSynchronizationTime = System.currentTimeMillis();
            for (Player viewer : getViewers()) {
                EntityTeleportPacket teleportPacket = new EntityTeleportPacket();
                teleportPacket.entityId = viewer.getEntityId();
                teleportPacket.x = viewer.getX();
                teleportPacket.y = viewer.getY();
                teleportPacket.z = viewer.getZ();
                teleportPacket.onGround = viewer.onGround;
                playerConnection.sendPacket(teleportPacket);
            }
        }
    }

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
        PlayerConnection connection = player.getPlayerConnection();
        String property = "eyJ0aW1lc3RhbXAiOjE1NjU0ODMwODQwOTYsInByb2ZpbGVJZCI6ImFiNzBlY2I0MjM0NjRjMTRhNTJkN2EwOTE1MDdjMjRlIiwicHJvZmlsZU5hbWUiOiJUaGVNb2RlOTExIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkOTE2NzJiNTE0MmJhN2Y3MjA2ZTRjN2IwOTBkNzhlM2Y1ZDc2NDdiNWFmZDIyNjFhZDk4OGM0MWI2ZjcwYTEifX19";
        SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
        spawnPlayerPacket.entityId = getEntityId();
        spawnPlayerPacket.playerUuid = getUuid();
        spawnPlayerPacket.x = x;
        spawnPlayerPacket.y = y;
        spawnPlayerPacket.z = z;
        spawnPlayerPacket.yaw = yaw;
        spawnPlayerPacket.pitch = pitch;

        PlayerInfoPacket pInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);
        PlayerInfoPacket.AddPlayer addP = new PlayerInfoPacket.AddPlayer(getUuid(), getUsername(), GameMode.CREATIVE, 10);
        PlayerInfoPacket.AddPlayer.Property p = new PlayerInfoPacket.AddPlayer.Property("textures", property);//new PlayerInfoPacket.AddPlayer.Property("textures", properties.get(onlinePlayer.getUsername()));
        addP.properties.add(p);
        pInfoPacket.playerInfos.add(addP);

        connection.sendPacket(pInfoPacket);
        connection.sendPacket(spawnPlayerPacket);

        for (EntityEquipmentPacket.Slot slot : EntityEquipmentPacket.Slot.values()) {
            syncEquipment(slot); // TODO only send packets to "player" and not all viewers
        }
    }

    @Override
    public void removeViewer(Player player) {
        super.removeViewer(player);
        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER);
        playerInfoPacket.playerInfos.add(new PlayerInfoPacket.RemovePlayer(getUuid()));
        player.playerConnection.sendPacket(playerInfoPacket);
    }

    public void sendBlockBreakAnimation(Position blockPosition, byte destroyStage) {
        BlockBreakAnimationPacket breakAnimationPacket = new BlockBreakAnimationPacket();
        breakAnimationPacket.entityId = getEntityId() + 1;
        breakAnimationPacket.blockPosition = blockPosition;
        breakAnimationPacket.destroyStage = destroyStage;
        playerConnection.sendPacket(breakAnimationPacket);
        sendPacketToViewers(breakAnimationPacket);
    }

    public void sendMessage(String message) {
        ChatMessagePacket chatMessagePacket = new ChatMessagePacket(Chat.rawText(message), ChatMessagePacket.Position.CHAT);
        playerConnection.sendPacket(chatMessagePacket);
    }

    public void teleport(double x, double y, double z) {
        PlayerPositionAndLookPacket positionAndLookPacket = new PlayerPositionAndLookPacket();
        positionAndLookPacket.x = x;
        positionAndLookPacket.y = y;
        positionAndLookPacket.z = z;
        positionAndLookPacket.yaw = getYaw();
        positionAndLookPacket.pitch = getPitch();
        positionAndLookPacket.flags = 0x00;
        positionAndLookPacket.teleportId = 67;
        getPlayerConnection().sendPacket(positionAndLookPacket);
    }

    public String getUsername() {
        return username;
    }

    public PlayerConnection getPlayerConnection() {
        return playerConnection;
    }

    public PlayerInventory getInventory() {
        return inventory;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void kick(String message) {
        DisconnectPacket disconnectPacket = new DisconnectPacket();
        disconnectPacket.message = message;
        playerConnection.sendPacket(disconnectPacket);
        playerConnection.getConnection().close();
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

    public void openInventory(Inventory inventory) {
        if (inventory == null)
            throw new IllegalArgumentException("Inventory cannot be null, use Player#closeInventory() to close current");

        if (getOpenInventory() != null) {
            getOpenInventory().removeViewer(this);
        }

        OpenWindowPacket openWindowPacket = new OpenWindowPacket();
        openWindowPacket.windowId = inventory.getUniqueId();
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
            closeWindowPacket.windowId = openInventory.getUniqueId();
            openInventory.removeViewer(this);
            refreshOpenInventory(null);
        }
        playerConnection.sendPacket(closeWindowPacket);
        inventory.update();
    }

    public void syncEquipment(EntityEquipmentPacket.Slot slot) {
        EntityEquipmentPacket equipmentPacket = new EntityEquipmentPacket();
        equipmentPacket.entityId = getEntityId();
        equipmentPacket.slot = slot;
        equipmentPacket.itemStack = inventory.getEquipment(slot);
        sendPacketToViewers(equipmentPacket);
    }

    public void addPacketToQueue(ClientPlayPacket packet) {
        synchronized (packets) {
            this.packets.add(packet);
        }
    }

    public void refreshGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
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

    public void refreshTargetBlock(CustomBlock targetCustomBlock, Position targetBlockPosition) {
        this.targetCustomBlock = targetCustomBlock;
        this.targetBlockPosition = targetBlockPosition;
        this.targetBlockTime = targetBlockPosition == null ? 0 : System.currentTimeMillis();
    }

    public void resetTargetBlock() {
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

    public void refreshVehicleSteer(float sideways, float forward) {
        this.sideways = sideways;
        this.forward = forward;
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public enum Hand {
        MAIN,
        OFF
    }
}
