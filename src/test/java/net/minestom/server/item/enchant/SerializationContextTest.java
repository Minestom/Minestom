package net.minestom.server.item.enchant;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.registry.TestRegistries;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class SerializationContextTest {

    @Test
    void testValueEffectSerializationVanilla() {
        var registry = ValueEffect.createDefaultRegistry();
        var coder = new RegistryTranscoder<>(Transcoder.NBT, new TestRegistries(r -> r.enchantmentValueEffects = registry), true, false);

        var result = assertOk(ValueEffect.CODEC.encode(coder, new ValueEffect.Add(new LevelBasedValue.Constant(1))));
        assertEqualsSNBT("""
                {"type":"minecraft:add","value":1f}
                """, result);
    }

    @Test
    void testValueEffectSerializationCustom() {
        var registry = ValueEffect.createDefaultRegistry();
        registry.register("minestom:my_effect", MyEffect.CODEC); // NOT registered to MINECRAFT_CORE
        var coder = new RegistryTranscoder<>(Transcoder.NBT, new TestRegistries(r -> r.enchantmentValueEffects = registry), true, false);

        var result = assertOk(ValueEffect.CODEC.encode(coder, new MyEffect()));
        assertNull(result);
    }

    @Test
    void testValueEffectSerializationCustomInList() {
        var registry = ValueEffect.createDefaultRegistry();
        registry.register("minestom:my_effect", MyEffect.CODEC); // NOT registered to MINECRAFT_CORE
        var coder = new RegistryTranscoder<>(Transcoder.NBT, new TestRegistries(r -> r.enchantmentValueEffects = registry), true, false);

        var result = assertOk(ValueEffect.CODEC.list().encode(coder, List.of(
                new ValueEffect.Add(new LevelBasedValue.Constant(1)),
                new MyEffect()
        )));
        assertEqualsSNBT("""
                [{"type":"minecraft:add","value":1f}]
                """, result);
    }

    @Test
    void testValueEffectSerializationCompoundCustom() {
        assumeFalse(true, "TODO(1.21.5)");
        var levelBasedValueRegistry = LevelBasedValue.createDefaultRegistry();
        var valueEffectRegistry = ValueEffect.createDefaultRegistry();
        levelBasedValueRegistry.register("minestom:my_level_based_value", MyLevelBasedValue.CODEC); // NOT registered to MINECRAFT_CORE
        var coder = new RegistryTranscoder<>(Transcoder.NBT, new TestRegistries(r -> {
            r.enchantmentLevelBasedValues = levelBasedValueRegistry;
            r.enchantmentValueEffects = valueEffectRegistry;
        }), true, false);

        var result = assertOk(ValueEffect.CODEC.encode(coder, new ValueEffect.Add(new MyLevelBasedValue())));
        assertNull(result); // Should get nothing because MyLevelBasedValue is missing and that would create an invalid Add
    }

    static class MyLevelBasedValue implements LevelBasedValue {
        public static final StructCodec<MyLevelBasedValue> CODEC = StructCodec.struct(MyLevelBasedValue::new);

        @Override
        public float calc(int level) {
            return 0;
        }

        @Override
        public StructCodec<MyLevelBasedValue> codec() {
            return CODEC;
        }
    }

    static class MyEffect implements ValueEffect {
        public static final StructCodec<MyEffect> CODEC = StructCodec.struct(MyEffect::new);

        @Override
        public float apply(float base, int level) {
            return 0;
        }

        @Override
        public StructCodec<MyEffect> codec() {
            return CODEC;
        }
    }

}
