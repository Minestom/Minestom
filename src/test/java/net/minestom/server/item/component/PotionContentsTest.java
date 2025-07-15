package net.minestom.server.item.component;

import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PotionContentsTest extends AbstractItemComponentTest<PotionContents> {

    @Override
    protected DataComponent<PotionContents> component() {
        return DataComponents.POTION_CONTENTS;
    }

    @Override
    protected List<Map.Entry<String, PotionContents>> directReadWriteEntries() {
        return List.of(
                Map.entry("empty", PotionContents.EMPTY),
                Map.entry("single effect", new PotionContents(PotionType.STRONG_SWIFTNESS)),
                Map.entry("single effect, color", new PotionContents(PotionType.STRONG_SWIFTNESS, new Color(0x123456))),
                Map.entry("custom effect", new PotionContents(new CustomPotionEffect(PotionEffect.INVISIBILITY, (byte) 2, 10, true, false, true))),
                Map.entry("custom effect recursive", new PotionContents(new CustomPotionEffect(PotionEffect.INVISIBILITY, new CustomPotionEffect.Settings(
                        (byte) 2, 10, true, false, true, new CustomPotionEffect.Settings(
                        (byte) 2, 10, true, false, true, null))))),
                Map.entry("custom effect", new PotionContents(List.of(
                        new CustomPotionEffect(PotionEffect.INVISIBILITY, (byte) 2, 10, true, false, true),
                        new CustomPotionEffect(PotionEffect.STRENGTH, (byte) 3, 10000, false, true, false)
                )))
        );
    }

    @Test
    void alternativeNbtSyntax() {
        var value = assertOk(DataComponents.POTION_CONTENTS.decode(Transcoder.NBT,
                StringBinaryTag.stringBinaryTag("minecraft:strong_swiftness")));
        var expected = new PotionContents(PotionType.STRONG_SWIFTNESS, null, List.of(), null);
        assertEquals(expected, value);
    }
}
