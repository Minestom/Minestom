package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickShiftPlayerSelfTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertPlayerSingleClick(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.AIR), ItemStack.AIR, Map.of());
    }

    @Test
    public void insertOne() {
        assertPlayerSingleClick(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.DIAMOND)),
                ItemStack.AIR, Map.of(9, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void armorEquip() {
        assertPlayerSingleClick(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.CHAINMAIL_HELMET)),
                ItemStack.AIR, Map.of(PlayerInventoryUtils.HELMET_SLOT, ItemStack.of(Material.CHAINMAIL_HELMET)));
    }

    @Test
    public void armorEquipFull() {
        assertPlayerSingleClick(inventory -> {
            inventory.setHelmet(ItemStack.of(Material.DIAMOND_HELMET));
            return ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.CHAINMAIL_HELMET));
        }, ItemStack.of(Material.CHAINMAIL_HELMET), Map.of());
    }

    @Test
    public void armorUnequip() {
        assertPlayerSingleClick(inventory -> {
            inventory.setHelmet(ItemStack.of(Material.CHAINMAIL_HELMET));
            return ClickProcessor.shiftWithinPlayer(inventory, PlayerInventoryUtils.HELMET_SLOT, ItemStack.of(Material.CHAINMAIL_HELMET));
        }, ItemStack.AIR, Map.of(9, ItemStack.of(Material.CHAINMAIL_HELMET)));
    }

    @Test
    public void armorUnequipFull() {
        assertPlayerSingleClick(inventory -> {
            for (int i = 0; i < 36; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            inventory.setHelmet(ItemStack.of(Material.CHAINMAIL_HELMET));
            return ClickProcessor.shiftWithinPlayer(inventory, PlayerInventoryUtils.HELMET_SLOT, ItemStack.of(Material.CHAINMAIL_HELMET));
        }, ItemStack.of(Material.CHAINMAIL_HELMET), Map.of());
    }

    @Test
    public void incrOne() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(9, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(9, ItemStack.of(Material.DIAMOND, 2)));
    }

    @Test
    public void insertSecondPart() {
        assertPlayerSingleClick(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 9, ItemStack.of(Material.DIAMOND)),
                ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void almostOverflow() {
        assertPlayerSingleClick(inventory -> {
            for (int i = 0; i < 8; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            return ClickProcessor.shiftWithinPlayer(inventory, 9, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(8, ItemStack.of(Material.DIAMOND)));
    }
}
