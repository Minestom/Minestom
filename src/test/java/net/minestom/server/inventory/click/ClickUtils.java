package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClickUtils {

    public static void assertClick(@NotNull ClickResult initialChanges, @NotNull ClickInfo info, @Nullable ClickResult expectedChanges) {
        var player = createPlayer();
        var inventory = createInventory();

        initialChanges.applyChanges(player, inventory);
        var changes = inventory.handleClick(player, info);
        assertEquals(expectedChanges, changes);
    }

    public static void assertPlayerClick(@NotNull ClickResult initialChanges, @NotNull ClickInfo info, @Nullable ClickResult expectedChanges) {
        var player = createPlayer();
        var inventory = player.getInventory();

        initialChanges.applyChanges(player, inventory);
        var changes = inventory.handleClick(player, info);
        assertEquals(expectedChanges, changes);
    }

    public static void assertProcessed(@NotNull ClickPreprocessor preprocessor, @NotNull Player player, @Nullable ClickInfo info, @NotNull ClientClickWindowPacket packet) {
        assertEquals(info, preprocessor.process(player, packet));
    }

    public static void assertProcessed(@NotNull Player player, @Nullable ClickInfo info, @NotNull ClientClickWindowPacket packet) {
        assertProcessed(new ClickPreprocessor(createInventory()), player, info, packet);
    }

    public static void assertProcessed(@Nullable ClickInfo info, @NotNull ClientClickWindowPacket packet) {
        assertProcessed(createPlayer(), info, packet);
    }

    public static @NotNull ClientClickWindowPacket clickPacket(@NotNull ClientClickWindowPacket.ClickType type, int windowId, int button, int slot) {
        return new ClientClickWindowPacket((byte) windowId, 0, (short) slot, (byte) button, type, List.of(), ItemStack.AIR);
    }

    public static @NotNull ClickPreprocessor createPreprocessor() {
        return new ClickPreprocessor(createInventory());
    }

    public static @NotNull AbstractInventory createInventory() {
        return new Inventory(InventoryType.HOPPER, "TestInventory");
    }

    public static @NotNull Player createPlayer() {
        return new Player(UUID.randomUUID(), "TestPlayer", new PlayerConnection() {
            @Override
            public void sendPacket(@NotNull SendablePacket packet) {}

            @Override
            public @NotNull SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public void disconnect() {}
        });
    }

}
