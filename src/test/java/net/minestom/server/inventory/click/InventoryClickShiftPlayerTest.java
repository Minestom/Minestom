package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickShiftPlayerTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertPlayerSingleClick(inventory -> ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.AIR)),
                ItemStack.AIR, Map.of());
    }

    @Test
    public void insertOne() {
        assertPlayerSingleClick(inventory -> ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND)),
                ItemStack.AIR, Map.of(8, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void incrOne() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(8, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(8, ItemStack.of(Material.DIAMOND, 2)));
    }

    @Test
    public void insertSecondPart() {
        assertPlayerSingleClick(inventory -> {
            for (int i = 0; i < 9; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            return ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(35, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void overflow() {
        assertPlayerSingleClick(inventory -> {
            for (int i = 0; i < 36; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            return ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND), Map.of());
    }

    @Test
    public void almostOverflow() {
        assertPlayerSingleClick(inventory -> {
            for (int i = 0; i < 35; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            return ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(35, ItemStack.of(Material.DIAMOND)));
    }
}
