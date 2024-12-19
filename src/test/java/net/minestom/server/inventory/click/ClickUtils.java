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

    public static @NotNull Player createPlayer() {
        return new Player(new PlayerConnection() {
            @Override
            public void sendPacket(@NotNull SendablePacket packet) {
            }

            @Override
            public @NotNull SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public void disconnect() {
            }
        }, new GameProfile(UUID.randomUUID(), "TestPlayer"));
    }

    public static void assertProcessed(@NotNull Click.Preprocessor preprocessor, @NotNull Player player, @Nullable Click.Info info, @NotNull ClientClickWindowPacket packet) {
        assertEquals(info, preprocessor.processClick(packet, player.getGameMode() == GameMode.CREATIVE, SIZE));
    }

    public static void assertProcessed(@NotNull Player player, @Nullable Click.Info info, @NotNull ClientClickWindowPacket packet) {
        assertProcessed(new Click.Preprocessor(), player, info, packet);
    }

    public static void assertProcessed(@Nullable Click.Info info, @NotNull ClientClickWindowPacket packet) {
        assertProcessed(createPlayer(), info, packet);
    }

    public static @NotNull ClientClickWindowPacket clickPacket(@NotNull ClientClickWindowPacket.ClickType type, int windowId, int button, int slot) {
        return new ClientClickWindowPacket((byte) windowId, 0, (short) slot, (byte) button, type, List.of(), ItemStack.AIR);
    }
}