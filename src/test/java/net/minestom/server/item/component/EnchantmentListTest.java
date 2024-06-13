package net.minestom.server.item.component;

import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.testing.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnchantmentListTest extends AbstractItemComponentTest<EnchantmentList> {
    // This is not a test, but it creates a compile error if the component type is changed away from Unit,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<EnchantmentList>> SHARED_COMPONENTS = List.of(
            ItemComponent.ENCHANTMENTS,
            ItemComponent.STORED_ENCHANTMENTS
    );

    @Override
    protected @NotNull DataComponent<EnchantmentList> component() {
        return SHARED_COMPONENTS.getFirst();
    }

    @Override
    protected @NotNull List<Map.Entry<String, EnchantmentList>> directReadWriteEntries() {
        return List.of(
                Map.entry("empty", EnchantmentList.EMPTY),
                Map.entry("single entry", new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1), true)),
                Map.entry("multi entry", new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1, Enchantment.PUNCH, 2), false))
        );
    }

    @Test
    void testShorthandNbtSyntax(Env env) throws Exception {
        var tag = TagStringIOExt.readTag("""
                {
                    "sharpness": 1,
                    "punch": 2,
                }
                """);
        var context = new BinaryTagSerializer.ContextWithRegistries(env.process());
        var value = component().read(context, tag);
        assertEquals(new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1, Enchantment.PUNCH, 2), true), value);
    }
}
