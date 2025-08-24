package net.minestom.server.item.component;

import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.testing.Env;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnchantmentListTest extends AbstractItemComponentTest<EnchantmentList> {
    // This is not a test, but it creates a compile error if the component type is changed away from Unit,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<EnchantmentList>> SHARED_COMPONENTS = List.of(
            DataComponents.ENCHANTMENTS,
            DataComponents.STORED_ENCHANTMENTS
    );

    @Override
    protected DataComponent<EnchantmentList> component() {
        return SHARED_COMPONENTS.getFirst();
    }

    @Override
    protected List<Map.Entry<String, EnchantmentList>> directReadWriteEntries() {
        return List.of(
                Map.entry("empty", EnchantmentList.EMPTY),
                Map.entry("single entry", new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1))),
                Map.entry("multi entry", new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1, Enchantment.PUNCH, 2)))
        );
    }

    @Test
    void testShorthandNbtSyntax(Env env) throws Exception {
        var tag = MinestomAdventure.tagStringIO().asTag("""
                {
                    "sharpness": 1,
                    "punch": 2,
                }
                """);
        var coder = new RegistryTranscoder<>(Transcoder.NBT, env.process());
        var value = assertOk(component().decode(coder, tag));
        assertEquals(new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1, Enchantment.PUNCH, 2)), value);
    }
}
