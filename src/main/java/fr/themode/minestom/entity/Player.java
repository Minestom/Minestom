package fr.themode.minestom.entity;

import fr.themode.minestom.Main;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.*;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Position;

import java.util.UUID;

public class Player extends LivingEntity {

    private boolean isSneaking;
    private boolean isSprinting;

    private long lastKeepAlive;

    private String username;
    private PlayerConnection playerConnection;

    private GameMode gameMode;
    private PlayerInventory inventory;
    private short heldSlot;
    private Inventory openInventory;

    private Position targetBlockPosition;
    private long targetBlockTime;

    // TODO set proper UUID
    public Player(UUID uuid, String username, PlayerConnection playerConnection) {
        this.uuid = uuid;
        this.username = username;
        this.playerConnection = playerConnection;

        this.inventory = new PlayerInventory(this);
    }

    @Override
    public void update() {

        // Target block stage
        if (instance != null && targetBlockPosition != null) {
            int timeBreak = 750; // In ms
            int animationCount = 10;
            long since = System.currentTimeMillis() - targetBlockTime;
            byte stage = (byte) (since / (timeBreak / animationCount));
            BlockBreakAnimationPacket breakAnimationPacket = new BlockBreakAnimationPacket();
            breakAnimationPacket.entityId = getEntityId() + 1;
            breakAnimationPacket.blockPosition = targetBlockPosition;
            breakAnimationPacket.destroyStage = stage;
            if (stage > 9) {
                instance.setBlock(targetBlockPosition.getX(), targetBlockPosition.getY(), targetBlockPosition.getZ(), (short) 0);
                refreshTargetBlock(null);
            }
            playerConnection.sendPacket(breakAnimationPacket); // TODO send to all online players
        }


        // Multiplayer sync
        EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
        entityTeleportPacket.entityId = getEntityId();
        entityTeleportPacket.x = x;
        entityTeleportPacket.y = y;
        entityTeleportPacket.z = z;
        entityTeleportPacket.yaw = yaw;
        entityTeleportPacket.pitch = pitch;
        entityTeleportPacket.onGround = true;
        for (Player onlinePlayer : Main.getConnectionManager().getOnlinePlayers()) {
            if (!onlinePlayer.equals(this))
                onlinePlayer.getPlayerConnection().sendPacket(entityTeleportPacket);
        }
        playerConnection.sendPacket(new UpdateViewPositionPacket(Math.floorDiv((int) x, 16), Math.floorDiv((int) z, 16)));
    }

    public void sendMessage(String message) {
        ChatMessagePacket chatMessagePacket = new ChatMessagePacket("{\"text\": \"" + message + "\"}", ChatMessagePacket.Position.CHAT);
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

    public ItemStack getHeldItemStack() {
        return inventory.getItemStack(heldSlot);
    }

    public Inventory getOpenInventory() {
        return openInventory;
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

    public void refreshGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void refreshView(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void refreshOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void refreshSneaking(boolean sneaking) {
        isSneaking = sneaking;
    }

    public void refreshSprinting(boolean sprinting) {
        isSprinting = sprinting;
    }

    public void refreshKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }

    public void refreshHeldSlot(short slot) {
        this.heldSlot = slot;
    }

    public void refreshOpenInventory(Inventory openInventory) {
        this.openInventory = openInventory;
    }

    public void refreshTargetBlock(Position targetBlockPosition) {
        this.targetBlockPosition = targetBlockPosition;
        this.targetBlockTime = targetBlockPosition == null ? 0 : System.currentTimeMillis();
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public enum Hand {
        MAIN,
        OFF
    }
}
