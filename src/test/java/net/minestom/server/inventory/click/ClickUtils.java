package net.minestom.server.inventory.click;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ClickUtils {
    public static final @NotNull InventoryType TYPE = InventoryType.HOPPER;

    public static final int SIZE = TYPE.getSize(); // Default hopper size

    public static @NotNull Inventory createInventory() {
        return new Inventory(TYPE, "TestInventory");
    }

    public static void assertProcessed(@NotNull ClickPreprocessor preprocessor, @Nullable Click info, @NotNull ClientClickWindowPacket packet) {
        assertEquals(info, preprocessor.processClick(packet, SIZE));
    }

    public static void assertProcessed(@Nullable Click info, @NotNull ClientClickWindowPacket packet) {
        assertProcessed(new ClickPreprocessor(), info, packet);
    }

    public static @NotNull ClientClickWindowPacket clickPacket(@NotNull ClientClickWindowPacket.ClickType type, int windowId, int button, int slot) {
        return new ClientClickWindowPacket((byte) windowId, 0, (short) slot, (byte) button, type, Map.of(), ItemStack.Hash.AIR);
    }
}