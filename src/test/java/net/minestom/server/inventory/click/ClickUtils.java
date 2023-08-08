package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.ContainerInventory;
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
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClickUtils {

    public static final @NotNull InventoryType TYPE = InventoryType.HOPPER;

    public static final int SIZE = TYPE.getSize(); // Default hopper size

    public static @NotNull Inventory createInventory() {
        return new ContainerInventory(TYPE, "TestInventory");
    }

    public static @NotNull Click.Preprocessor createPreprocessor() {
        return new Click.Preprocessor();
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

    public static void assertClick(@NotNull UnaryOperator<Click.Setter> initialChanges, @NotNull Click.Info info, @NotNull UnaryOperator<Click.Setter> expectedChanges) {
        var player = createPlayer();
        var inventory = createInventory();

        ContainerInventory.apply(initialChanges.apply(new Click.Setter(inventory.getSize())).build(), player, inventory);
        var changes = inventory.handleClick(player, info);
        assertEquals(expectedChanges.apply(new Click.Setter(inventory.getSize())).build(), changes);
    }

    public static void assertPlayerClick(@NotNull UnaryOperator<Click.Setter> initialChanges, @NotNull Click.Info info, @NotNull UnaryOperator<Click.Setter> expectedChanges) {
        var player = createPlayer();
        var inventory = player.getInventory();

        ContainerInventory.apply(initialChanges.apply(new Click.Setter(inventory.getSize())).build(), player, inventory);
        var changes = inventory.handleClick(player, info);
        assertEquals(expectedChanges.apply(new Click.Setter(inventory.getSize())).build(), changes);
    }

    public static void assertProcessed(@NotNull Click.Preprocessor preprocessor, @NotNull Player player, @Nullable Click.Info info, @NotNull ClientClickWindowPacket packet) {
        assertEquals(info, preprocessor.process(packet, createInventory(), player.isCreative()));
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
