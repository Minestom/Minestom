package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.ContainerInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ClickUtils {
    public static final @NotNull InventoryType TYPE = InventoryType.HOPPER;

    public static final int SIZE = TYPE.getSize(); // Default hopper size

    public static @NotNull Inventory createInventory() {
        return new ContainerInventory(TYPE, "TestInventory");
    }

    public static @NotNull Click.Preprocessor createPreprocessor() {
        return new Click.Preprocessor();
    }

    public static @NotNull ItemStack magic(int amount) {
        return ItemStack.of(Material.STONE, amount);
    }

    public static @NotNull ItemStack magic2(int amount) {
        return ItemStack.of(Material.DIRT, amount);
    }

    public static @NotNull Player createPlayer() {
        return new Player(UUID.randomUUID(), "TestPlayer", new PlayerConnection() {
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
        });
    }

    public static void assertClick(@NotNull List<Click.Change> initial, @NotNull Click.Info info, @NotNull List<Click.Change> expected) {
        var player = createPlayer();
        var inventory = createInventory();

        ContainerInventory.apply(initial, player, inventory);
        var actual = inventory.handleClick(player, info);

        assertChanges(expected, actual, inventory.getSize());
    }

    public static void assertPlayerClick(@NotNull List<Click.Change> initial, @NotNull Click.Info info, @NotNull List<Click.Change> expected) {
        var player = createPlayer();
        var inventory = player.getInventory();

        ContainerInventory.apply(initial, player, inventory);
        var actual = inventory.handleClick(player, info);

        assertChanges(expected, actual, inventory.getSize());
    }

    public static void assertChanges(List<Click.Change> expected, List<Click.Change> actual, int size) {
        assertEquals(ChangeResult.make(expected, size), ChangeResult.make(actual, size));
    }

    private record ChangeResult(Map<Integer, ItemStack> main, Map<Integer, ItemStack> player,
                                @Nullable ItemStack cursor, List<ItemStack> drops) {
        private static ChangeResult make(@NotNull List<Click.Change> changes, int size) {
            Map<Integer, ItemStack> main = new HashMap<>();
            Map<Integer, ItemStack> player = new HashMap<>();
            @Nullable ItemStack cursor = null;
            List<ItemStack> drops = new ArrayList<>();

            for (var change : changes) {
                switch (change) {
                    case Click.Change.Main(int slot, ItemStack item) -> {
                        if (slot < size) {
                            main.put(slot, item);
                        } else {
                            player.put(PlayerInventoryUtils.protocolToMinestom(slot, size), item);
                        }
                    }
                    case Click.Change.Player(int slot, ItemStack item) -> player.put(slot, item);
                    case Click.Change.Cursor(ItemStack item) -> cursor = item;
                    case Click.Change.DropFromPlayer(ItemStack item) -> drops.add(item);
                }
            }

            return new ChangeResult(main, player, cursor, drops);
        }

    }

    public static void assertProcessed(@NotNull Click.Preprocessor preprocessor, @NotNull Player player, @Nullable Click.Info info, @NotNull ClientClickWindowPacket packet) {
        assertEquals(info, preprocessor.processContainerClick(packet, createInventory().getSize(), player.isCreative()));
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
