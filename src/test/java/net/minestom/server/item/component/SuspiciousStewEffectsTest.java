package net.minestom.server.item.component;

import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuspiciousStewEffectsTest extends AbstractItemComponentTest<SuspiciousStewEffects> {

    @Override
    protected @NotNull DataComponent<SuspiciousStewEffects> component() {
        return ItemComponent.SUSPICIOUS_STEW_EFFECTS;
    }

    @Override
    protected @NotNull List<Map.Entry<String, SuspiciousStewEffects>> directReadWriteEntries() {
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
        var value = ItemComponent.SUSPICIOUS_STEW_EFFECTS.read(BinaryTagSerializer.Context.EMPTY, TagStringIOExt.readTag("""
                [{"id": "minecraft:strength"}]
                """));
        var expected = new SuspiciousStewEffects(new SuspiciousStewEffects.Effect(PotionEffect.STRENGTH, 160));
        assertEquals(expected, value);
    }
}
