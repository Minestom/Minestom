package net.minestom.server.entity.fakeplayer;

import net.minestom.server.entity.Entity;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryModifier;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

/**
 * This class act as a client controller for {@link FakePlayer}
 */
public class FakePlayerController {

    private FakePlayer fakePlayer;

    public FakePlayerController(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    /**
     * Make the player write a message
     *
     * @param message the message to write
     */
    public void sendChatMessage(String message) {
        ClientChatMessagePacket chatMessagePacket = new ClientChatMessagePacket();
        chatMessagePacket.message = message;
        addToQueue(chatMessagePacket);
    }

    public void clickWindow(boolean playerInventory, short slot, byte button, short action, int mode) {
        Inventory inventory = playerInventory ? null : fakePlayer.getOpenInventory();
        InventoryModifier inventoryModifier = inventory == null ? fakePlayer.getInventory() : inventory;
        playerInventory = inventoryModifier instanceof PlayerInventory;

        slot = playerInventory ? (short) PlayerInventoryUtils.convertToPacketSlot(slot) : slot;

        ItemStack itemStack = inventoryModifier.getItemStack(slot);

        ClientClickWindowPacket clickWindowPacket = new ClientClickWindowPacket();
        clickWindowPacket.windowId = playerInventory ? 0 : inventory.getWindowId();
        clickWindowPacket.slot = slot;
        clickWindowPacket.button = button;
        clickWindowPacket.actionNumber = action;
        clickWindowPacket.mode = mode;
        clickWindowPacket.item = itemStack;
        addToQueue(clickWindowPacket);
    }

    public void closeWindow() {
        Inventory openInventory = fakePlayer.getOpenInventory();

        ClientCloseWindow closeWindow = new ClientCloseWindow();
        closeWindow.windowId = openInventory == null ? 0 : openInventory.getWindowId();
        addToQueue(closeWindow);
    }

    public void sendPluginMessage(String channel, byte[] message) {
        ClientPluginMessagePacket pluginMessagePacket = new ClientPluginMessagePacket();
        pluginMessagePacket.channel = channel;
        pluginMessagePacket.data = message;
        addToQueue(pluginMessagePacket);
    }

    public void attackEntity(Entity entity) {
        ClientInteractEntityPacket interactEntityPacket = new ClientInteractEntityPacket();
        interactEntityPacket.targetId = entity.getEntityId();
        interactEntityPacket.type = ClientInteractEntityPacket.Type.ATTACK;
        addToQueue(interactEntityPacket);
    }

    /**
     * Make the player receives a packet
     * WARNING: pretty much unsafe, used internally to redirect packets here,
     * you should instead use {@link net.minestom.server.network.player.PlayerConnection#sendPacket(ServerPacket)}
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

    private void addToQueue(ClientPlayPacket clientPlayPacket) {
        this.fakePlayer.addPacketToQueue(clientPlayPacket);
    }
}
