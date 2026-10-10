package net.minestom.server.world.attribute;

import net.minestom.server.codec.Transcoder;
import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.IntProvider;
import net.minestom.server.utils.WeightedList;
import net.minestom.server.world.attribute.MobSpawnSettings.MobCategory;
import net.minestom.server.world.attribute.MobSpawnSettings.MobSpawnCost;
import net.minestom.server.world.attribute.MobSpawnSettings.SpawnerData;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RegistriesTest
public class MobSpawnSettingsRegistriesTest {
    private static final WeightedList<SpawnerData> SHEEP = WeightedList.of(
            new WeightedList.Entry<>(new SpawnerData(EntityType.SHEEP, new IntProvider.Constant(4)), 12));
    private static final WeightedList<SpawnerData> ZOMBIES = WeightedList.of(
            new WeightedList.Entry<>(new SpawnerData(EntityType.ZOMBIE, new IntProvider.Uniform(2, 4)), 90));

    @Test
    public void codecRoundTrip(Registries registries) {
        var coder = new RegistryTranscoder<>(Transcoder.JSON, registries);
        var settings = new MobSpawnSettings(
                Map.of(MobCategory.CREATURE, SHEEP, MobCategory.MONSTER, ZOMBIES, MobCategory.AMBIENT, WeightedList.of()),
                Map.of(EntityType.ENDERMAN, new MobSpawnCost(0.12, 1.0)));
        var encoded = assertOk(MobSpawnSettings.CODEC.encode(coder, settings));
        assertEquals(settings, assertOk(MobSpawnSettings.CODEC.decode(coder, encoded)));
    }

    @Test
    public void overlayKeepsInheritedCategories() {
        var inherited = new MobSpawnSettings(Map.of(MobCategory.CREATURE, SHEEP),
                Map.of(EntityType.ENDERMAN, new MobSpawnCost(0.12, 1.0)));
        var argument = new MobSpawnSettings(Map.of(MobCategory.MONSTER, ZOMBIES),
                Map.of(EntityType.ENDERMAN, new MobSpawnCost(0.5, 0.7)));
        var result = MobSpawnSettingsModifier.OVERLAY.modify(inherited, argument);
        assertEquals(Map.of(MobCategory.CREATURE, SHEEP, MobCategory.MONSTER, ZOMBIES), result.spawnsByCategory());
        assertEquals(Map.of(EntityType.ENDERMAN, new MobSpawnCost(0.5, 0.7)), result.spawnCosts());
    }

    @Test
    public void overlayReplacesCoveredCategories() {
        var inherited = new MobSpawnSettings(Map.of(MobCategory.CREATURE, SHEEP), Map.of());
        var argument = new MobSpawnSettings(Map.of(MobCategory.CREATURE, WeightedList.of(), MobCategory.MONSTER, ZOMBIES), Map.of());
        assertEquals(argument, MobSpawnSettingsModifier.OVERLAY.modify(inherited, argument));
        assertEquals(inherited, MobSpawnSettingsModifier.OVERLAY.modify(inherited, MobSpawnSettings.EMPTY));
        assertEquals(argument, MobSpawnSettingsModifier.OVERLAY.modify(MobSpawnSettings.EMPTY, argument));
    }

    @Test
    public void plainsBiomeOverlaysSpawns(Registries registries) {
        Biome plains = registries.biome().get(Biome.PLAINS);
        assertNotNull(plains);
        var entry = plains.attributes().entries().get(EnvironmentAttribute.NATURAL_MOB_SPAWNS);
        assertNotNull(entry);
        assertInstanceOf(MobSpawnSettingsModifier.class, entry.modifier());
        var settings = assertInstanceOf(MobSpawnSettings.class, entry.argument());
        assertTrue(settings.spawnsByCategory().containsKey(MobCategory.CREATURE));
    }
}
