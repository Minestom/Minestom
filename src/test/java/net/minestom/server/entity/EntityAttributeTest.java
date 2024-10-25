package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.utils.NamespaceID;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityAttributeTest {

    @Test
    public void testEntityUpdatesAttributes(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        LivingEntity entity = new LivingEntity(EntityTypes.CHICKEN);
        entity.setInstance(instance).join();

        double baseHealth = 20;
        double addition = 10;

        double baseAmount = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
        assertEquals(0, Double.compare(baseAmount, baseHealth)); // Avoid floating-point rounding issues

        ItemStack itemStack = ItemStack.builder(Material.DIAMOND).set(ItemComponent.ATTRIBUTE_MODIFIERS,
                new AttributeList(new AttributeList.Modifier(Attribute.MAX_HEALTH,
                        new AttributeModifier(NamespaceID.from("minestom:health"), addition, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.HEAD))).build();

        entity.setBoots(itemStack);
        assertEquals(0, Double.compare(entity.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth)); // No change since we are in the wrong slot
        entity.setHelmet(itemStack);
        assertEquals(0, Double.compare(entity.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth + addition)); // Should change
        entity.setHelmet(ItemStack.AIR);
        assertEquals(0, Double.compare(entity.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth)); // Reset back to base
    }

    @Test
    public void testPlayerUpdatesAttributes(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 1));

        double baseHealth = 20;
        double addition = 10;

        double baseAmount = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        assertEquals(0, Double.compare(baseAmount, baseHealth)); // Avoid floating-point rounding issues

        ItemStack itemStack = ItemStack.builder(Material.DIAMOND).set(ItemComponent.ATTRIBUTE_MODIFIERS,
                new AttributeList(new AttributeList.Modifier(Attribute.MAX_HEALTH,
                        new AttributeModifier(NamespaceID.from("minestom:health"), addition, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND))).build();

        player.setBoots(itemStack);
        assertEquals(0, Double.compare(player.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth)); // No change since we are in the wrong slot
        player.setItemInMainHand(itemStack);
        assertEquals(0, Double.compare(player.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth + addition)); // Should change
        player.refreshHeldSlot((byte) 1);
        assertEquals(0, Double.compare(player.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth)); // Changes since the player switched the main hand item
        player.refreshHeldSlot((byte) 0);
        assertEquals(0, Double.compare(player.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth + addition)); // Switched back
        player.setItemInMainHand(ItemStack.AIR);
        assertEquals(0, Double.compare(player.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth));
    }

    @Test
    public void testDirectlyAddAttributes(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 1));

        double baseHealth = 20;
        double addition = 10;
        // Don't compare against base health first (that will initialize the attribute, and we want to make sure we don't error when we add an item with attribute modifiers)

        ItemStack itemStack = ItemStack.builder(Material.DIAMOND).set(ItemComponent.ATTRIBUTE_MODIFIERS,
                new AttributeList(new AttributeList.Modifier(Attribute.MAX_HEALTH,
                        new AttributeModifier(NamespaceID.from("minestom:health"), addition, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND))).build();

        player.setItemInMainHand(itemStack);
        assertEquals(0, Double.compare(player.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth + addition));
    }
}
