package net.minestom.server.entity.fakeplayer;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * This class acts as a client controller for {@link FakePlayer}.
 * <p>
 * The main use is to simulate the receiving of {@link ClientPlayPacket}
 */
public class FakePlayerController {

    private final FakePlayer fakePlayer;

    /**
     * Initializes a new {@link FakePlayerController} with the given {@link FakePlayer}.
     *
     * @param fakePlayer The fake player that should used the controller.
     */
    public FakePlayerController(@NotNull FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    /**
     * Simulates a click in a window.
     *
     * @param playerInventory {@code true} if the window a {@link PlayerInventory}, otherwise {@code false}.
     * @param slot            The slot where the fake player should click on.
     * @param button          The mouse button that the fake player should used.
     * @param clickType       The click type
     */
    public void clickWindow(boolean playerInventory, short slot, byte button, ClientClickWindowPacket.ClickType clickType) {
        Inventory inventory = playerInventory ? null : fakePlayer.getOpenInventory();
        AbstractInventory abstractInventory = inventory == null ? fakePlayer.getInventory() : inventory;
        playerInventory = abstractInventory instanceof PlayerInventory;

        slot = playerInventory ? (short) PlayerInventoryUtils.convertToPacketSlot(slot) : slot;

        ItemStack itemStack = abstractInventory.getItemStack(slot);

        ClientClickWindowPacket clickWindowPacket = new ClientClickWindowPacket();
        clickWindowPacket.windowId = playerInventory ? 0 : inventory.getWindowId();
        clickWindowPacket.slot = slot;
        clickWindowPacket.button = button;
        clickWindowPacket.clickType = clickType;
        clickWindowPacket.item = itemStack;
        addToQueue(clickWindowPacket);
    }

    /**
     * Closes the current opened inventory if there is any.
     */
    public void closeWindow() {
        Inventory openInventory = fakePlayer.getOpenInventory();

        ClientCloseWindowPacket closeWindow = new ClientCloseWindowPacket();
        closeWindow.windowId = openInventory == null ? 0 : openInventory.getWindowId();
        addToQueue(closeWindow);
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel The channel of the message.
     * @param message The message data.
     */
    public void sendPluginMessage(String channel, byte[] message) {
        ClientPluginMessagePacket pluginMessagePacket = new ClientPluginMessagePacket();
        pluginMessagePacket.channel = channel;
        pluginMessagePacket.data = message;
        addToQueue(pluginMessagePacket);
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel The channel of the message.
     * @param message The message data.
     */
    public void sendPluginMessage(String channel, String message) {
        sendPluginMessage(channel, message.getBytes());
    }

    /**
     * Attacks the given {@code entity}.
     *
     * @param entity The entity that is to be attacked.
     */
    public void attackEntity(Entity entity) {
        ClientInteractEntityPacket interactEntityPacket = new ClientInteractEntityPacket();
        interactEntityPacket.targetId = entity.getEntityId();
        interactEntityPacket.type = ClientInteractEntityPacket.Type.ATTACK;
        addToQueue(interactEntityPacket);
    }

    /**
     * Respawns the player.
     *
     * @see Player#respawn()
     */
    public void respawn() {
        // Sending the respawn packet for some reason
        // Is related to FakePlayer#showPlayer and the tablist option (probably because of the scheduler)
        /*ClientStatusPacket statusPacket = new ClientStatusPacket();
        statusPacket.action = ClientStatusPacket.Action.PERFORM_RESPAWN;
        addToQueue(statusPacket);*/
        fakePlayer.respawn();
    }

    /**
     * Changes the current held slot for the player.
     *
     * @param slot The slot that the player has to held.
     * @throws IllegalArgumentException If {@code slot} is not between {@code 0} and {@code 8}.
     */
    public void setHeldItem(short slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "Slot has to be between 0 and 8!");

        ClientHeldItemChangePacket heldItemChangePacket = new ClientHeldItemChangePacket();
        heldItemChangePacket.slot = slot;
        addToQueue(heldItemChangePacket);
    }

    /**
     * Sends an animation packet that animates the specified arm.
     *
     * @param hand The hand of the arm to be animated.
     */
    public void sendArmAnimation(Player.Hand hand) {
        ClientAnimationPacket animationPacket = new ClientAnimationPacket();
        animationPacket.hand = hand;
        addToQueue(animationPacket);
    }

    /**
     * Uses the item in the given {@code hand}.
     *
     * @param hand The hand in which an ite mshould be.
     */
    public void useItem(Player.Hand hand) {
        ClientUseItemPacket useItemPacket = new ClientUseItemPacket();
        useItemPacket.hand = hand;
        addToQueue(useItemPacket);
    }

    /**
     * Rotates the fake player.
     *
     * @param yaw   The new yaw for the fake player.
     * @param pitch The new pitch for the fake player.
     */
    public void rotate(float yaw, float pitch) {
        ClientPlayerRotationPacket playerRotationPacket = new ClientPlayerRotationPacket();
        playerRotationPacket.yaw = yaw;
        playerRotationPacket.pitch = pitch;
        playerRotationPacket.onGround = fakePlayer.isOnGround();
        addToQueue(playerRotationPacket);
    }

    /**
     * Starts the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    public void startDigging(Point blockPosition, BlockFace blockFace) {
        ClientPlayerDiggingPacket playerDiggingPacket = new ClientPlayerDiggingPacket();
        playerDiggingPacket.status = ClientPlayerDiggingPacket.Status.STARTED_DIGGING;
        playerDiggingPacket.blockPosition = blockPosition;
        playerDiggingPacket.blockFace = blockFace;
        addToQueue(playerDiggingPacket);
    }

    /**
     * Stops the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    public void stopDigging(Point blockPosition, BlockFace blockFace) {
        ClientPlayerDiggingPacket playerDiggingPacket = new ClientPlayerDiggingPacket();
        playerDiggingPacket.status = ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING;
        playerDiggingPacket.blockPosition = blockPosition;
        playerDiggingPacket.blockFace = blockFace;
        addToQueue(playerDiggingPacket);
    }

    /**
     * Finishes the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    public void finishDigging(Point blockPosition, BlockFace blockFace) {
        ClientPlayerDiggingPacket playerDiggingPacket = new ClientPlayerDiggingPacket();
        playerDiggingPacket.status = ClientPlayerDiggingPacket.Status.FINISHED_DIGGING;
        playerDiggingPacket.blockPosition = blockPosition;
        playerDiggingPacket.blockFace = blockFace;
        addToQueue(playerDiggingPacket);
    }

    /**
     * Makes the player receives a packet
     * WARNING: pretty much unsafe, used internally to redirect packets here,
     * you should instead use {@link PlayerConnection#sendPacket(ServerPacket)}
     *
     * @param serverPacket the packet to consume
     */
    public void consumePacket(ServerPacket serverPacket) {
        if (serverPacket instanceof PlayerPositionAndLookPacket) {
            ClientTeleportConfirmPacket teleportConfirmPacket = new ClientTeleportConfirmPacket();
            teleportConfirmPacket.teleportId = ((PlayerPositionAndLookPacket) serverPacket).teleportId;
            addToQueue(teleportConfirmPacket);
        } else if (serverPacket instanceof KeepAlivePacket) {
            ClientKeepAlivePacket keepAlivePacket = new ClientKeepAlivePacket();
            keepAlivePacket.id = ((KeepAlivePacket) serverPacket).id;
            addToQueue(keepAlivePacket);
        }
    }

    /**
     * All packets in the queue are executed in the {@link Player#update(long)} method. It is used internally to add all
     * received packet from the client. Could be used to "simulate" a received packet, but to use at your own risk!
     *
     * @param clientPlayPacket The packet to add in the queue.
     */
    private void addToQueue(ClientPlayPacket clientPlayPacket) {
        this.fakePlayer.addPacketToQueue(clientPlayPacket);
    }
}
