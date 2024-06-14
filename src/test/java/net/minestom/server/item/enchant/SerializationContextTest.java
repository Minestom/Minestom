package net.minestom.server.item.enchant;

import net.minestom.server.registry.TestRegistries;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertNull;

class SerializationContextTest {

    @Test
    void testValueEffectSerializationVanilla() {
        var registry = ValueEffect.createDefaultRegistry();
        var context = new BinaryTagSerializer.ContextWithRegistries(new TestRegistries(r -> r.enchantmentValueEffects = registry), true);

        var result = ValueEffect.NBT_TYPE.write(context, new ValueEffect.Add(new LevelBasedValue.Constant(1)));
        assertEqualsSNBT("""
                {"type":"minecraft:add","value":1f}
                """, result);
    }

    @Test
    void testValueEffectSerializationCustom() {
        var registry = ValueEffect.createDefaultRegistry();
        registry.register("minestom:my_effect", MyEffect.NBT_TYPE); // NOT registered to MINECRAFT_CORE
        var context = new BinaryTagSerializer.ContextWithRegistries(new TestRegistries(r -> r.enchantmentValueEffects = registry), true);

        var result = ValueEffect.NBT_TYPE.write(context, new MyEffect());
        assertNull(result);
    }

    @Test
    void testValueEffectSerializationCustomInList() {
        var registry = ValueEffect.createDefaultRegistry();
        registry.register("minestom:my_effect", MyEffect.NBT_TYPE); // NOT registered to MINECRAFT_CORE
        var context = new BinaryTagSerializer.ContextWithRegistries(new TestRegistries(r -> r.enchantmentValueEffects = registry), true);

        var result = ValueEffect.NBT_TYPE.list().write(context, List.of(
                new ValueEffect.Add(new LevelBasedValue.Constant(1)),
                new MyEffect()
        ));
        assertEqualsSNBT("""
                [{"type":"minecraft:add","value":1f}]
                """, result);
    }

    @Test
    void testValueEffectSerializationCompoundCustom() {
        var levelBasedValueRegistry = LevelBasedValue.createDefaultRegistry();
        var valueEffectRegistry = ValueEffect.createDefaultRegistry();
        levelBasedValueRegistry.register("minestom:my_level_based_value", MyLevelBasedValue.NBT_TYPE); // NOT registered to MINECRAFT_CORE
        var context = new BinaryTagSerializer.ContextWithRegistries(new TestRegistries(r -> {
            r.enchantmentLevelBasedValues = levelBasedValueRegistry;
            r.enchantmentValueEffects = valueEffectRegistry;
        }), true);

        var result = ValueEffect.NBT_TYPE.write(context, new ValueEffect.Add(new MyLevelBasedValue()));
        assertNull(result); // Should get nothing because MyLevelBasedValue is missing and that would create an invalid Add
    }

    static class MyLevelBasedValue implements LevelBasedValue {
        public static final BinaryTagSerializer<MyLevelBasedValue> NBT_TYPE = BinaryTagSerializer.UNIT.map(v -> new MyLevelBasedValue(), v -> Unit.INSTANCE);
        @Override
        public float calc(int level) {
            return 0;
        }
        @Override
        public @NotNull BinaryTagSerializer<MyLevelBasedValue> nbtType() {
            return NBT_TYPE;
        }
    }

    static class MyEffect implements ValueEffect {
        public static final BinaryTagSerializer<MyEffect> NBT_TYPE = BinaryTagSerializer.UNIT.map(v -> new MyEffect(), v -> Unit.INSTANCE);
        @Override
        public float apply(float base, int level) {
            return 0;
        }
        @Override
        public @NotNull BinaryTagSerializer<MyEffect> nbtType() {
            return NBT_TYPE;
        }
    }

}
