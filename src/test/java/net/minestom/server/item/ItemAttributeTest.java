package net.minestom.server.item;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemAttributeTest {

    @Test
    public void attribute() {
        var attributes = List.of(new ItemAttribute(
                new UUID(0, 0), "generic.attack_damage", Attribute.ATTACK_DAMAGE,
                AttributeOperation.ADDITION, 2, AttributeSlot.MAINHAND));
        var item = ItemStack.builder(Material.STICK)
                .meta(builder -> builder.attributes(attributes))
                .build();
        assertEquals(attributes, item.meta().getAttributes());
    }

    @Test
    public void attributeNbt() {
        var item = ItemStack.builder(Material.STICK)
                .meta(builder -> builder.attributes(
                        List.of(new ItemAttribute(
                                new UUID(0, 0), "generic.attack_damage", Attribute.ATTACK_DAMAGE,
                                AttributeOperation.ADDITION, 2, AttributeSlot.MAINHAND))))
                .build();
        assertEqualsSNBT("""
                {"AttributeModifiers":[
                {
                "Amount":2.0D,
                "UUID":[I;0,0,0,0],
                "Slot":"mainhand",
                "Operation":0,
                "AttributeName":"generic.attack_damage",
                "Name":"generic.attack_damage"
                }
                ]}
                """, item.meta().toNBT());
    }
}
