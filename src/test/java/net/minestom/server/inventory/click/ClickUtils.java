package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
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

    private static @NotNull AbstractInventory createInventory() {
        return new Inventory(InventoryType.HOPPER, "TestInventory");
    }

    private static @NotNull Player createPlayer() {
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
