package net.minestom.server.item.component;

import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuspiciousStewEffectsTest extends AbstractItemComponentTest<SuspiciousStewEffects> {

    @Override
    protected DataComponent<SuspiciousStewEffects> component() {
        return DataComponents.SUSPICIOUS_STEW_EFFECTS;
    }

    @Override
    protected List<Map.Entry<String, SuspiciousStewEffects>> directReadWriteEntries() {
        return List.of(
                Map.entry("empty", SuspiciousStewEffects.EMPTY),
                Map.entry("single", new SuspiciousStewEffects(new SuspiciousStewEffects.Effect(PotionEffect.ABSORPTION, 100))),
                Map.entry("multi", new SuspiciousStewEffects(List.of(
                        new SuspiciousStewEffects.Effect(PotionEffect.ABSORPTION, 100),
                        new SuspiciousStewEffects.Effect(PotionEffect.STRENGTH, 2)
                )))
        );
    }

    @Test
    void nbtReadDefaultDuration() throws Exception {
        var value = assertOk(DataComponents.SUSPICIOUS_STEW_EFFECTS.decode(Transcoder.NBT, MinestomAdventure.tagStringIO().asTag("""
                [{"id": "minecraft:strength"}]
                """)));
        var expected = new SuspiciousStewEffects(new SuspiciousStewEffects.Effect(PotionEffect.STRENGTH, 160));
        assertEquals(expected, value);
    }
}
