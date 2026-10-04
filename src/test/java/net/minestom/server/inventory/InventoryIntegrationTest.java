package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.inventory.InventoryBundleItemSelectEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientSelectBundleItemPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.SetCursorItemPacket;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeProperty;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InventoryIntegrationTest {

    private static final ItemStack MAGIC_STACK = ItemStack.of(Material.DIAMOND, 3);

    @Test
    public void setSlotDuplicateTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setItemStack(3, MAGIC_STACK);
        packetTracker.assertSingle(slot -> assertEquals(MAGIC_STACK, slot.itemStack())); // Setting a slot should send a packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setItemStack(3, MAGIC_STACK);
        packetTracker.assertEmpty(); // Setting the same slot to the same ItemStack should not send another packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setItemStack(3, ItemStack.AIR);
        packetTracker.assertSingle(slot -> assertEquals(ItemStack.AIR, slot.itemStack())); // Setting a slot should send a packet
    }

    @Test
    public void setCursorItemDuplicateTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var packetTracker = connection.trackIncoming(SetCursorItemPacket.class);
        player.getInventory().setCursorItem(MAGIC_STACK);
        packetTracker.assertSingle(slot -> assertEquals(MAGIC_STACK, slot.itemStack())); // Setting a slot should send a packet

        packetTracker = connection.trackIncoming(SetCursorItemPacket.class);
        player.getInventory().setCursorItem(MAGIC_STACK);
        packetTracker.assertEmpty(); // Setting the same slot to the same ItemStack should not send another packet

        packetTracker = connection.trackIncoming(SetCursorItemPacket.class);
        player.getInventory().setCursorItem(ItemStack.AIR);
        packetTracker.assertSingle(slot -> assertEquals(ItemStack.AIR, slot.itemStack())); // Setting a slot should send a packet
    }

    @Test
    public void clearInventoryTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var setSlotTracker = connection.trackIncoming(SetSlotPacket.class);
        var setCursorTracker = connection.trackIncoming(SetCursorItemPacket.class);

        inventory.setItemStack(1, MAGIC_STACK);
        inventory.setItemStack(3, MAGIC_STACK);
        inventory.setItemStack(19, MAGIC_STACK);
        inventory.setItemStack(40, MAGIC_STACK);
        player.getInventory().setCursorItem(MAGIC_STACK);

        setSlotTracker.assertCount(4);
        setCursorTracker.assertCount(1);

        setSlotTracker = connection.trackIncoming(SetSlotPacket.class);
        var updateWindowTracker = connection.trackIncoming(WindowItemsPacket.class);
        var equipmentTracker = connection.trackIncoming(EntityEquipmentPacket.class);

        // Perform the clear operation we are testing
        inventory.clear();

        // Make sure not individual SetSlotPackets get sent
        setSlotTracker.assertEmpty();

        // Make sure WindowItemsPacket is empty except for cursor (clearing the player inventory itself clears the cursor)
        updateWindowTracker.assertSingle(windowItemsPacket -> {
            assertEquals(MAGIC_STACK, windowItemsPacket.carriedItem());
            for (ItemStack item : windowItemsPacket.items()) {
                assertEquals(ItemStack.AIR, item);
            }
        });

        // Make sure EntityEquipmentPacket isn't sent (this is an Inventory, not a PlayerInventory)
        equipmentTracker.assertEmpty();
    }

    @Test
    public void clearingPlayerInventoryClearsCursorTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        var setCursorTracker = connection.trackIncoming(SetCursorItemPacket.class);
        player.getInventory().setCursorItem(MAGIC_STACK);
        setCursorTracker.assertCount(1);

        var setSlotTracker = connection.trackIncoming(SetSlotPacket.class);
        var setPlayerSlotTracker = connection.trackIncoming(SetPlayerInventorySlotPacket.class);
        setCursorTracker = connection.trackIncoming(SetCursorItemPacket.class);
        var updateWindowTracker = connection.trackIncoming(WindowItemsPacket.class);
        var equipmentTracker = connection.trackIncoming(EntityEquipmentPacket.class);

        // Perform the clear operation we are testing
        player.getInventory().clear();

        // Make sure no individual set slot/set cursor/set player slot packets get sent
        setSlotTracker.assertEmpty();
        setPlayerSlotTracker.assertEmpty();
        setCursorTracker.assertEmpty();

        // Make sure WindowItemsPacket is empty
        updateWindowTracker.assertSingle(windowItemsPacket -> assertEquals(ItemStack.AIR, windowItemsPacket.carriedItem()));

        // Make sure no EntityEquipmentPacket is sent (nothing was equipped)
        equipmentTracker.assertEmpty();
    }

    @Test
    public void closeInventoryTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        final var inventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        player.openInventory(inventory);
        assertSame(inventory, player.getOpenInventory());
        player.closeInventory();
        assertNull(player.getOpenInventory());
    }

    @Test
    public void openInventoryOnItemDropFromInventoryClosingTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        var listener = env.listen(ItemDropEvent.class);
        final var firstInventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        player.openInventory(firstInventory);
        assertSame(firstInventory, player.getOpenInventory());
        player.getInventory().setCursorItem(ItemStack.of(Material.STONE));

        listener.followup();
        player.closeInventory();
        assertNull(player.getOpenInventory());

        player.openInventory(firstInventory);
        player.getInventory().setCursorItem(ItemStack.of(Material.STONE));
        final var secondInventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        listener.followup(event -> event.getPlayer().openInventory(secondInventory));
        player.closeInventory();
        assertSame(secondInventory, player.getOpenInventory());
    }

    @Test
    public void testInnerInventorySlotSending(Env env) {
        // Inner inventory changes are sent along with the open inventory
        // Otherwise, they are sent separately

        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        // Ensure that slots not in the inner inventory are sent separately
        var packetTracker = connection.trackIncoming(SetPlayerInventorySlotPacket.class);
        player.getInventory().setItemStack(PlayerInventoryUtils.OFFHAND_SLOT, MAGIC_STACK);
        packetTracker.assertSingle(slot -> {
            assertEquals(40, slot.slot()); // Off hand is slot 40 in player inventory
            assertEquals(MAGIC_STACK, slot.itemStack());
        });

        // Ensure that inner inventory slots are sent as the opened inventory
        packetTracker = connection.trackIncoming(SetPlayerInventorySlotPacket.class);
        player.getInventory().setItemStack(0, MAGIC_STACK); // Test with first inner inventory slot
        packetTracker.assertSingle(slot -> {
            assertEquals(0, slot.slot());
            assertEquals(MAGIC_STACK, slot.itemStack());
        });

        packetTracker = connection.trackIncoming(SetPlayerInventorySlotPacket.class);
        player.getInventory().setItemStack(35, MAGIC_STACK); // Test with last inner inventory slot
        packetTracker.assertSingle(slot -> {
            assertEquals(35, slot.slot());
            assertEquals(MAGIC_STACK, slot.itemStack());
        });
    }

    @Test
    public void testEventNode(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        AtomicBoolean called = new AtomicBoolean(false);
        inventory.eventNode().addListener(
                InventoryOpenEvent.class,
                event -> {
                    assertSame(inventory, event.getInventory());
                    called.set(true);
                }
        );

        player.openInventory(inventory);
        assertTrue(called.get(), "InventoryOpenEvent not fired");
    }

    @Test
    public void bundleActivateTestLowerInventory(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        final var inventory = new Inventory(InventoryType.CHEST_6_ROW, "title");
        player.openInventory(inventory);
        // No bundles are set - they might not exist serverside and be sent directly to the client

        var listenerLowerInv = env.trackEvent(InventoryBundleItemSelectEvent.class, EventFilter.PLAYER, player);
        player.addPacketToQueue(new ClientSelectBundleItemPacket(inventory.getSize() + 2, 42));
        player.interpretPacketQueue();
        listenerLowerInv.assertSingle(event -> {
            assertEquals(11, event.getSlot());
            assertEquals(player.getInventory(), event.getInventory());
        });
    }

    @Test
    public void bundleActivateTestUpperInventory(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        final var inventory = new Inventory(InventoryType.CHEST_3_ROW, "title"); // size = 27
        player.openInventory(inventory);

        var listener = env.trackEvent(InventoryBundleItemSelectEvent.class, EventFilter.PLAYER, player);
        // Window slot 27 (27 + 0) corresponds to player inventory main slot 9 in Minestom
        player.addPacketToQueue(new ClientSelectBundleItemPacket(27, 42));
        player.interpretPacketQueue();

        listener.assertSingle(event -> {
            assertEquals(9, event.getSlot()); // Expected: slot 9 (main inventory slot 9)
            assertEquals(player.getInventory(), event.getInventory());
        });
    }

    @Test
    public void bundleActivateTestNoContainerOpen(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var listener = env.trackEvent(InventoryBundleItemSelectEvent.class, EventFilter.PLAYER, player);
        // Window slot 36 corresponds to hotbar slot 0 in Minestom (when openInventory == null)
        player.addPacketToQueue(new ClientSelectBundleItemPacket(36, 42));
        player.interpretPacketQueue();

        listener.assertSingle(event -> {
            assertEquals(0, event.getSlot()); // Expected: slot 0 (hotbar slot 0)
            assertEquals(player.getInventory(), event.getInventory());
        });
    }

    @Test
    public void bundleActivateTestInventorySizeSlot(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        var listener = env.trackEvent(InventoryBundleItemSelectEvent.class, EventFilter.PLAYER, player);
        // Window slot 46 corresponds to player inventory size slot (which is 46)
        player.addPacketToQueue(new ClientSelectBundleItemPacket(46, 42));
        player.interpretPacketQueue();

        listener.assertSingle(event -> {
            assertEquals(player.getInventory().getSize(), event.getSlot());
            assertEquals(player.getInventory(), event.getInventory());
        });
    }

    @Test
    public void bundleActivateTestInventorySizeSlotContainer(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        final var inventory = new Inventory(InventoryType.CHEST_3_ROW, "title");
        player.openInventory(inventory);

        var listener = env.trackEvent(InventoryBundleItemSelectEvent.class, EventFilter.PLAYER, player);
        // Window slot 64 translates to player inventory size slot (which is 46)
        player.addPacketToQueue(new ClientSelectBundleItemPacket(64, 42));
        player.interpretPacketQueue();

        listener.assertSingle(event -> {
            assertEquals(player.getInventory().getSize(), event.getSlot());
            assertEquals(player.getInventory(), event.getInventory());
        });
    }

    @ParameterizedTest
    @MethodSource("furnaceTypes")
    public void anyFurnaceShiftClickMovesValidInputIntoInputSlot(
            InventoryType inventoryType,
            RecipeProperty property,
            Env env
    ) {
        var context = createFurnaceContext(env, inventoryType);

        registerRecipe(
                property,
                Material.BEEF
        );

        shiftClick(context.player(), context.inventory(), 0, 30, Material.BEEF);

        assertEquals(ItemStack.of(Material.BEEF), context.inventory().getItemStack(0));
    }

    private static Stream<Arguments> furnaceTypes() {
        return Stream.of(
                Arguments.of(
                        InventoryType.FURNACE,
                        RecipeProperty.FURNACE_INPUT
                ),
                Arguments.of(
                        InventoryType.BLAST_FURNACE,
                        RecipeProperty.BLAST_FURNACE_INPUT
                ),
                Arguments.of(
                        InventoryType.SMOKER,
                        RecipeProperty.SMOKER_INPUT
                )
        );
    }

    @Test
    public void furnaceShiftClickMovesFuelIntoFuelSlot(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        shiftClick(context.player(), context.inventory(), 0, 30, Material.COAL);

        assertEquals(ItemStack.AIR, context.inventory().getItemStack(0));
        assertEquals(ItemStack.of(Material.COAL), context.inventory().getItemStack(1));
        assertEquals(ItemStack.AIR, context.inventory().getItemStack(2));
    }

    @Test
    public void furnaceShiftClickPrefersInputOverFuel(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        // Coal is fuel, but explicitly make it a valid furnace input as well.
        registerRecipe(
                RecipeProperty.FURNACE_INPUT,
                Material.COAL
        );

        shiftClick(context.player(), context.inventory(), 0, 30, Material.COAL);

        assertEquals(ItemStack.of(Material.COAL), context.inventory().getItemStack(0));
        assertEquals(ItemStack.AIR, context.inventory().getItemStack(1));
    }

    @Test
    public void furnaceShiftClickFallsBackToFuelWhenInputSlotIsFull(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        registerRecipe(
                RecipeProperty.FURNACE_INPUT,
                Material.COAL
        );

        context.inventory().setItemStack(
                0,
                ItemStack.of(Material.COAL, 64)
        );

        shiftClick(context.player(), context.inventory(), 0, 30, Material.COAL);

        assertEquals(ItemStack.of(Material.COAL, 64), context.inventory().getItemStack(0));
        assertEquals(ItemStack.of(Material.COAL), context.inventory().getItemStack(1));
    }

    @Test
    public void furnaceShiftClickFallsBackToPlayerInventoryWhenInputSlotIsFull(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        registerRecipe(
                RecipeProperty.FURNACE_INPUT,
                Material.COBBLESTONE
        );

        context.inventory().setItemStack(
                0,
                ItemStack.of(Material.COBBLESTONE, 64)
        );

        shiftClick(context.player(), context.inventory(), 0, 30, Material.COBBLESTONE);

        // Hotbar -> main inventory fallback
        assertEquals(ItemStack.AIR, context.player().getInventory().getItemStack(0));
        assertEquals(
                ItemStack.of(Material.COBBLESTONE),
                context.player().getInventory().getItemStack(9)
        );

        assertEquals(ItemStack.of(Material.COBBLESTONE, 64), context.inventory().getItemStack(0));
        assertEquals(ItemStack.AIR, context.inventory().getItemStack(1));
    }

    @Test
    public void furnaceShiftClickFallsBackToPlayerInventoryWhenFuelSlotIsFull(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        context.inventory().setItemStack(
                1,
                ItemStack.of(Material.COAL, 64)
        );

        shiftClick(context.player(), context.inventory(), 0, 30, Material.COAL);

        // Hotbar -> main inventory fallback
        assertEquals(ItemStack.AIR, context.player().getInventory().getItemStack(0));
        assertEquals(
                ItemStack.of(Material.COAL),
                context.player().getInventory().getItemStack(9)
        );

        assertEquals(ItemStack.of(Material.COAL, 64), context.inventory().getItemStack(1));
    }

    @Test
    public void furnaceShiftClickMovesNonInputFromHotbarToMainInventory(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        shiftClick(context.player(), context.inventory(), 0, 30, Material.DIAMOND);

        assertEquals(ItemStack.AIR, context.player().getInventory().getItemStack(0));
        assertEquals(
                ItemStack.of(Material.DIAMOND),
                context.player().getInventory().getItemStack(9)
        );

        assertEquals(ItemStack.AIR, context.inventory().getItemStack(0));
        assertEquals(ItemStack.AIR, context.inventory().getItemStack(1));
        assertEquals(ItemStack.AIR, context.inventory().getItemStack(2));
    }

    @Test
    public void furnaceShiftClickMovesNonInputFromMainInventoryToHotbar(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        // Player inventory slot 9 is the first main-inventory slot.
        // In a furnace window it corresponds to window slot 3.
        shiftClick(context.player(), context.inventory(), 9, 3, Material.DIAMOND);

        assertEquals(ItemStack.AIR, context.player().getInventory().getItemStack(9));
        assertEquals(
                ItemStack.of(Material.DIAMOND),
                context.player().getInventory().getItemStack(0)
        );

        assertEquals(ItemStack.AIR, context.inventory().getItemStack(0));
        assertEquals(ItemStack.AIR, context.inventory().getItemStack(1));
        assertEquals(ItemStack.AIR, context.inventory().getItemStack(2));
    }

    @Test
    public void furnaceShiftClickIgnoresRecipesWithoutFurnaceInputProperty(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        MinecraftServer.getRecipeManager().addRecipe(new Recipe() {
            @Override
            public @NotNull Map<RecipeProperty, List<Material>> itemProperties() {
                return Map.of(
                        RecipeProperty.SMOKER_INPUT,
                        List.of(Material.COBBLESTONE)
                );
            }
        });

        shiftClick(context.player(), context.inventory(), 0, 30, Material.COBBLESTONE);

        assertEquals(ItemStack.AIR, context.inventory().getItemStack(0));

        // Falls back from hotbar into the regular inventory.
        assertEquals(
                ItemStack.of(Material.COBBLESTONE),
                context.player().getInventory().getItemStack(9)
        );
    }

    @Test
    public void furnaceShiftClickFindsInputAfterRecipeWithoutMatchingProperty(Env env) {
        var context = createFurnaceContext(env, InventoryType.FURNACE);

        MinecraftServer.getRecipeManager().addRecipe(new Recipe() {
            @Override
            public @NotNull Map<RecipeProperty, List<Material>> itemProperties() {
                return Map.of(
                        RecipeProperty.SMOKER_INPUT,
                        List.of(Material.COBBLESTONE)
                );
            }
        });

        registerRecipe(
                RecipeProperty.FURNACE_INPUT,
                Material.COBBLESTONE
        );

        shiftClick(context.player(), context.inventory(), 0, 30, Material.COBBLESTONE);

        assertEquals(
                ItemStack.of(Material.COBBLESTONE),
                context.inventory().getItemStack(0)
        );
    }


    private static FurnaceContext createFurnaceContext(Env env, InventoryType inventoryType) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        var inventory = new Inventory(inventoryType, "title");

        player.openInventory(inventory);

        return new FurnaceContext(player, inventory);
    }

    private static void registerRecipe(RecipeProperty property, Material material) {
        MinecraftServer.getRecipeManager().addRecipe(new Recipe() {
            @Override
            public @NotNull Map<RecipeProperty, List<Material>> itemProperties() {
                return Map.of(property, List.of(material));
            }
        });
    }

    private static void shiftClick(
            Player player,
            Inventory inventory,
            int playerSlot,
            int windowSlot,
            Material material
    ) {
        var item = ItemStack.of(material);

        player.getInventory().setItemStack(playerSlot, item);

        player.addPacketToQueue(new ClientClickWindowPacket(
                inventory.getWindowId(),
                0,
                (short) windowSlot,
                (byte) 0,
                ClientClickWindowPacket.ClickType.QUICK_MOVE,
                Map.of(),
                ItemStack.Hash.of(item, MinecraftServer.getRegistries())
        ));

        player.interpretPacketQueue();
    }

    private record FurnaceContext(
            Player player,
            Inventory inventory
    ) {
    }
}
