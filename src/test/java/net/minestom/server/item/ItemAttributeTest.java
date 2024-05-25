package net.minestom.server.item;

import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.attribute.VanillaAttribute;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.tag.TagHandler;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
class ItemAttributeTest {

    @Test
    void attribute(Env env) {
        var attributes = List.of(new ItemAttribute(
                new UUID(0, 0), "generic.attack_damage", VanillaAttribute.GENERIC_ATTACK_DAMAGE,
                AttributeOperation.ADDITION, 2, AttributeSlot.MAINHAND));
        var item = ItemStack.builder(Material.STICK)
                .meta(builder -> builder.attributes(attributes))
                .build();
        assertEquals(attributes, item.meta().getAttributes());
    }

    @Test
    void attributeReader(Env env) {
        env.process().attribute().loadVanillaAttributes();
        var attributes = List.of(new ItemAttribute(
                new UUID(0, 0), "generic.attack_damage", VanillaAttribute.GENERIC_ATTACK_DAMAGE,
                AttributeOperation.ADDITION, 2, AttributeSlot.MAINHAND));

        TagHandler handler = TagHandler.newHandler();
        handler.setTag(ItemTags.ATTRIBUTES, attributes);
        var item = ItemStack.fromNBT(Material.STICK, handler.asCompound());

        assertEquals(attributes, item.meta().getAttributes());
    }

    @Test
    void attributeNbt(Env env) {
        var item = ItemStack.builder(Material.STICK)
                .meta(builder -> builder.attributes(
                        List.of(new ItemAttribute(
                                new UUID(0, 0), "generic.attack_damage", VanillaAttribute.GENERIC_ATTACK_DAMAGE,
                                AttributeOperation.ADDITION, 2, AttributeSlot.MAINHAND))))
                .build();
        assertEqualsSNBT("""
                {"AttributeModifiers":[
                {
                "Amount":2.0D,
                "UUID":[I;0,0,0,0],
                "Slot":"mainhand",
                "Operation":0,
                "AttributeName":"minecraft:generic.attack_damage",
                "Name":"generic.attack_damage"
                }
                ]}
                """, item.meta().toNBT());
    }
}
