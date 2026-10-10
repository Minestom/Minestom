package net.minestom.server.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.generation.BlockStateProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicRegistryImplTest {

    // Horrible cyclic issue from where the keys are positioned to data dependence
    private static DynamicRegistryImpl<BlockStateProvider> load(String json) {
        var registry = new DynamicRegistryImpl<>(BuiltinRegistries.BLOCK_STATE_PROVIDER.key(), BlockStateProvider.REGISTRY_CODEC);
        var registries = new TestRegistries(r -> r.blockStateProvider = registry);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        registry.loadJsonEntries(registries, null, BlockStateProvider.REGISTRY_CODEC, root);
        return registry;
    }

    @Test
    public void forwardReferenceResolves() {
        var registry = load("""
                {
                  "test:derived": {"type": "minecraft:copy_properties", "source": "test:base"},
                  "test:base": {"id": "minecraft:stone"}
                }
                """);
        assertEquals(2, registry.size());
        var base = registry.getKey(Key.key("test:base"));
        assertEquals(new BlockStateProvider.Simple(Block.STONE), registry.get(base));
        assertEquals(new BlockStateProvider.CopyProperties(base), registry.get(Key.key("test:derived")));
    }

    @Test
    public void cycleFails() {
        var exception = assertThrows(IllegalStateException.class, () -> load("""
                {
                  "test:a": {"type": "minecraft:copy_properties", "source": "test:b"},
                  "test:b": {"type": "minecraft:copy_properties", "source": "test:a"}
                }
                """));
        assertTrue(exception.getMessage().startsWith("Failed to decode registry entry test:a for registry minecraft:worldgen/block_state_provider"), exception.getMessage());
        assertTrue(exception.getMessage().contains("Unknown key test:b"), exception.getMessage());
    }

    @Test
    public void brokenEntryFails() {
        var exception = assertThrows(IllegalStateException.class, () -> load("""
                {
                  "test:base": {"id": "minecraft:stone"},
                  "test:broken": {"type": "minecraft:missing"}
                }
                """));
        assertTrue(exception.getMessage().startsWith("Failed to decode registry entry test:broken"), exception.getMessage());
    }
}
