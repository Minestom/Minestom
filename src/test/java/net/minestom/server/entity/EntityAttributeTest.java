package net.minestom.server.entity;

import net.minestom.server.component.DataComponents;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityAttributeTest {

    @Test
    public void testEntityUpdatesAttributes(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        LivingEntity entity = new LivingEntity(EntityTypes.CHICKEN);
        entity.setInstance(instance).join();

        double addition = 10;

        double baseHealth = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
        assertEquals(0, Double.compare(baseHealth, entity.getAttributeValue(Attribute.MAX_HEALTH))); // Avoid floating-point rounding issues

        ItemStack itemStack = ItemStack.builder(Material.DIAMOND).set(DataComponents.ATTRIBUTE_MODIFIERS,
                new AttributeList(new AttributeList.Modifier(Attribute.MAX_HEALTH,
                        new AttributeModifier(Key.key("minestom:health"), addition, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.HEAD))).build();

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

        double addition = 10;

        double baseHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        assertEquals(0, Double.compare(baseHealth, player.getAttributeValue(Attribute.MAX_HEALTH))); // Avoid floating-point rounding issues

        ItemStack itemStack = ItemStack.builder(Material.DIAMOND).set(DataComponents.ATTRIBUTE_MODIFIERS,
                new AttributeList(new AttributeList.Modifier(Attribute.MAX_HEALTH,
                        new AttributeModifier(Key.key("minestom:health"), addition, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND))).build();

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

        ItemStack itemStack = ItemStack.builder(Material.DIAMOND).set(DataComponents.ATTRIBUTE_MODIFIERS,
                new AttributeList(new AttributeList.Modifier(Attribute.MAX_HEALTH,
                        new AttributeModifier(Key.key("minestom:health"), addition, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND))).build();

        player.setItemInMainHand(itemStack);
        assertEquals(0, Double.compare(player.getAttribute(Attribute.MAX_HEALTH).getValue(), baseHealth + addition));
    }

    @Test
    public void testEntityDefaultAttributes() {
        var ironGolem = new EntityCreature(EntityType.IRON_GOLEM);
        var zombie = new EntityCreature(EntityType.ZOMBIE);

        var golemHealth = ironGolem.getAttribute(Attribute.MAX_HEALTH);
        assertNotNull(golemHealth);
        assertEquals(100.0, golemHealth.getBaseValue(), 0.001);
        assertEquals(golemHealth.getBaseValue(), ironGolem.getAttributeValue(Attribute.MAX_HEALTH), 0.001);

        var zombieHealth = zombie.getAttribute(Attribute.MAX_HEALTH);
        assertNotNull(zombieHealth);
        assertEquals(20.0, zombieHealth.getBaseValue(), 0.001);
        assertEquals(zombieHealth.getBaseValue(), zombie.getAttributeValue(Attribute.MAX_HEALTH), 0.001);
    }

    @Test
    public void testEntitySpawnsWithCorrectHealth() {
        var ironGolem = new EntityCreature(EntityType.IRON_GOLEM);
        assertEquals(100.0f, ironGolem.getHealth(), 0.001f);
    }

    @Test
    public void testDefaultAttributesFromRegistry() {
        var golemDefaults = EntityType.IRON_GOLEM.defaultAttributes();
        assertEquals(100.0, golemDefaults.getOrDefault(Attribute.MAX_HEALTH, Double.NaN), 0.001);
        assertEquals(0.25, golemDefaults.getOrDefault(Attribute.MOVEMENT_SPEED, Double.NaN), 0.001);

        var zombieDefaults = EntityType.ZOMBIE.defaultAttributes();
        assertEquals(3.0, zombieDefaults.getOrDefault(Attribute.ATTACK_DAMAGE, Double.NaN), 0.001);
        assertEquals(20.0, zombieDefaults.getOrDefault(Attribute.MAX_HEALTH, Double.NaN), 0.001);

        assertTrue(EntityType.AREA_EFFECT_CLOUD.defaultAttributes().isEmpty());
    }

    @Test
    public void testDefaultAttributesAreImmutable() {
        var defaults = EntityType.IRON_GOLEM.defaultAttributes();
        assertThrows(UnsupportedOperationException.class, () -> defaults.put(Attribute.MAX_HEALTH, 1.0));
    }
}
