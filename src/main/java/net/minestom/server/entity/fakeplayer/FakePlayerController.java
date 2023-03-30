package net.minestom.server.entity.fakeplayer;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class acts as a client controller for {@link FakePlayer}.
 * <p>
 * The main use is to simulate the receiving of {@link ClientPacket}
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

        addToQueue(new ClientClickWindowPacket(playerInventory ? 0 : inventory.getWindowId(), 0,
                slot, button, clickType,
                List.of(), itemStack));
    }

    /**
     * Closes the current opened inventory if there is any.
     */
    public void closeWindow() {
        Inventory openInventory = fakePlayer.getOpenInventory();
        addToQueue(new ClientCloseWindowPacket(openInventory == null ? 0 : openInventory.getWindowId()));
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel The channel of the message.
     * @param message The message data.
     */
    public void sendPluginMessage(String channel, byte[] message) {
        addToQueue(new ClientPluginMessagePacket(channel, message));
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
        addToQueue(new ClientInteractEntityPacket(entity.getEntityId(), new ClientInteractEntityPacket.Attack(), fakePlayer.isSneaking()));
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
        addToQueue(new ClientHeldItemChangePacket(slot));
    }

    /**
     * Sends an animation packet that animates the specified arm.
     *
     * @param hand The hand of the arm to be animated.
     */
    public void sendArmAnimation(Player.Hand hand) {
        addToQueue(new ClientAnimationPacket(hand));
    }

    /**
     * Uses the item in the given {@code hand}.
     *
     * @param hand The hand in which an ite mshould be.
     */
    public void useItem(Player.Hand hand) {
        addToQueue(new ClientUseItemPacket(hand, 0));
    }

    /**
     * Rotates the fake player.
     *
     * @param yaw   The new yaw for the fake player.
     * @param pitch The new pitch for the fake player.
     */
    public void rotate(float yaw, float pitch) {
        addToQueue(new ClientPlayerRotationPacket(yaw, pitch, fakePlayer.isOnGround()));
    }

    /**
     * Starts the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    public void startDigging(Point blockPosition, BlockFace blockFace) {
        addToQueue(new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.STARTED_DIGGING, blockPosition, blockFace, 0));
    }

    /**
     * Stops the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    public void stopDigging(Point blockPosition, BlockFace blockFace) {
        addToQueue(new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING, blockPosition, blockFace, 0));
    }

    /**
     * Finishes the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    public void finishDigging(Point blockPosition, BlockFace blockFace) {
        addToQueue(new ClientPlayerDiggingPacket(ClientPlayerDiggingPacket.Status.FINISHED_DIGGING, blockPosition, blockFace, 0));
    }

    /**
     * Makes the player receives a packet
     * WARNING: pretty much unsafe, used internally to redirect packets here,
     * you should instead use {@link PlayerConnection#sendPacket(SendablePacket)}
     *
     * @param serverPacket the packet to consume
     */
    public void consumePacket(ServerPacket serverPacket) {
        if (serverPacket instanceof PlayerPositionAndLookPacket playerPositionAndLookPacket) {
            addToQueue(new ClientTeleportConfirmPacket(playerPositionAndLookPacket.teleportId()));
        } else if (serverPacket instanceof KeepAlivePacket keepAlivePacket) {
            addToQueue(new ClientKeepAlivePacket(keepAlivePacket.id()));
        }
    }

    /**
     * All packets in the queue are executed in the {@link Player#update(long)} method. It is used internally to add all
     * received packet from the client. Could be used to "simulate" a received packet, but to use at your own risk!
     *
     * @param clientPlayPacket The packet to add in the queue.
     */
    private void addToQueue(ClientPacket clientPlayPacket) {
        this.fakePlayer.addPacketToQueue(clientPlayPacket);
    }
}
