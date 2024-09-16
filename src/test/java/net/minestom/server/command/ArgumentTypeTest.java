package net.minestom.server.command;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.particle.Particle;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Range;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentTypeTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testArgumentEntityType() {
        var arg = ArgumentType.EntityType("entity_type");
        assertInvalidArg(arg, "minecraft:invalid_entity_type");
        assertArg(arg, EntityType.ARMOR_STAND, EntityType.ARMOR_STAND.name());
        assertArg(arg, EntityType.PLAYER, EntityType.PLAYER.name());
    }

    @Test
    public void testArgumentParticle() {
        var arg = ArgumentType.Particle("particle");
        assertInvalidArg(arg, "minecraft:invalid_particle");
        assertArg(arg, Particle.BLOCK, Particle.BLOCK.name());
        assertArg(arg, Particle.TOTEM_OF_UNDYING, Particle.TOTEM_OF_UNDYING.name());
    }

    @Test
    public void testArgumentBlockState() {
        var arg = ArgumentType.BlockState("block_state");
        assertInvalidArg(arg, "minecraft:invalid_block[invalid_property=invalid_key]");
        assertInvalidArg(arg, "minecraft:stone[invalid_property=invalid_key]");
        assertInvalidArg(arg, "minecraft:kelp[age=invalid_key]");

        assertArg(arg, Block.COBBLESTONE, "minecraft:cobblestone");
        assertArg(arg, Block.KELP.withProperty("age", "14"), "minecraft:kelp[age=14]");
    }

    @Test
    public void testArgumentColor() {
        var arg = ArgumentType.Color("color");
        assertInvalidArg(arg, "invalid_color");
        assertArg(arg, Style.style(NamedTextColor.DARK_PURPLE), "dark_purple");
        assertArg(arg, Style.empty(), "reset");
    }

    @Test
    public void testArgumentComponent() {
        var arg = ArgumentType.Component("component");
        var component1 = Component.text("Example text", NamedTextColor.DARK_AQUA);
        var component2 = Component.text("Other example text", Style.style(TextDecoration.OBFUSCATED));
        var json1 = GsonComponentSerializer.gson().serialize(component1);
        var json2 = GsonComponentSerializer.gson().serialize(component2);

        assertInvalidArg(arg, "invalid component");
        assertArg(arg, component1, json1);
        assertArg(arg, component2, json2);
    }

    @Test
    public void testArgumentEntity() {
        var arg = ArgumentType.Entity("entity");

        assertValidArg(arg, "@a");
        assertValidArg(arg, "@p");
        assertInvalidArg(arg, "@x");

        assertValidArg(arg, "@e[type=sheep]");
        assertValidArg(arg, "@e[type=!cow]");
        assertInvalidArg(arg, "@e[type=invalid_entity]");
        assertInvalidArg(arg, "@e[type=!invalid_entity_two]");

        assertValidArg(arg, "@e[gamemode=creative]");
        assertValidArg(arg, "@e[gamemode=!survival]");
        assertInvalidArg(arg, "@e[gamemode=invalid_gamemode]");
        assertInvalidArg(arg, "@e[gamemode=!invalid_gamemode_2]");

        assertValidArg(arg, "@e[limit=500]");
        assertInvalidArg(arg, "@e[limit=-500]");
        assertInvalidArg(arg, "@e[limit=invalid_integer]");
        assertInvalidArg(arg, "@e[limit=2147483648]");

        assertValidArg(arg, "@e[sort=nearest]");
        assertInvalidArg(arg, "@e[sort=invalid_sort]");

        assertValidArg(arg, "@e[level=55]");
        assertValidArg(arg, "@e[level=100..500]");
        assertInvalidArg(arg, "@e[level=20-50]");
        assertInvalidArg(arg, "@e[level=2147483648]");

        assertValidArg(arg, "@e[distance=500]");
        assertValidArg(arg, "@e[distance=50..150]");
        assertInvalidArg(arg, "@e[distance=-500-500]");
        assertInvalidArg(arg, "@e[distance=2147483648]");
    }

    @Test
    public void testArgumentFloatRange() {
        var arg = ArgumentType.FloatRange("float_range");
        assertArg(arg, new Range.Float(0f, 50f), "0..50");
        assertArg(arg, new Range.Float(0f, 0f), "0..0");
        assertArg(arg, new Range.Float(-50f, 0f), "-50..0");
        assertArg(arg, new Range.Float(-Float.MAX_VALUE, 50f), "..50");
        assertArg(arg, new Range.Float(0f, Float.MAX_VALUE), "0..");
        assertArg(arg, new Range.Float(-Float.MAX_VALUE, Float.MAX_VALUE), "-3.4028235E38..3.4028235E38");
        assertArg(arg, new Range.Float(0.5f, 24f), "0.5..24");
        assertArg(arg, new Range.Float(12f, 45.6f), "12..45.6");
        assertInvalidArg(arg, "..");
        assertInvalidArg(arg, "0..50..");
    }

    @Test
    public void testArgumentIntRange() {
        var arg = ArgumentType.IntRange("int_range");

        assertArg(arg, new Range.Int(0, 50), "0..50");
        assertArg(arg, new Range.Int(0, 0), "0..0");
        assertArg(arg, new Range.Int(-50, 0), "-50..0");
        assertArg(arg, new Range.Int(Integer.MIN_VALUE, 50), "..50");
        assertArg(arg, new Range.Int(0, Integer.MAX_VALUE), "0..");
        assertArg(arg, new Range.Int(Integer.MIN_VALUE, Integer.MAX_VALUE), "-2147483648..2147483647");

        assertInvalidArg(arg, "..");
        assertInvalidArg(arg, "-2147483649..2147483647");
        assertInvalidArg(arg, "-2147483648..2147483648");
        assertInvalidArg(arg, "0..50..");
        assertInvalidArg(arg, "0.5..24");
        assertInvalidArg(arg, "12..45.6");
    }

    @Test
    public void testArgumentItemStack() {
        var arg = ArgumentType.ItemStack("item_stack");
        assertArg(arg, ItemStack.AIR, "air");
        assertArg(arg, ItemStack.of(Material.GLASS_PANE).withTag(Tag.String("tag"), "value"), "glass_pane{tag:value}");
        assertArg(arg, ItemStack.of(Material.GLASS_PANE).with(ItemComponent.CUSTOM_MODEL_DATA, 5), "glass_pane[custom_model_data=5]");
        assertArg(arg, ItemStack.of(Material.GLASS_PANE).with(ItemComponent.CUSTOM_MODEL_DATA, 5).withTag(Tag.String("tag"), "value"), "glass_pane[custom_model_data=5]{tag:value}");
        assertArg(arg, ItemStack.of(Material.GLASS_PANE).with(ItemComponent.CUSTOM_MODEL_DATA, 5).with(ItemComponent.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder().putInt("hi", 232).build())).withTag(Tag.String("tag"), "value"),
                "glass_pane[custom_model_data=5,minecraft:custom_data={hi:232}]{tag:value}");
    }

    @Test
    public void testArgumentNbtCompoundTag() {
        var arg = ArgumentType.NbtCompound("nbt_compound");
        assertArg(arg, CompoundBinaryTag.builder().putLongArray("long_array", new long[]{12, 49, 119}).build(),
                "{\"long_array\":[L;12L,49L,119L]}");
        assertArg(arg, CompoundBinaryTag.builder().put("nested", CompoundBinaryTag.builder().putIntArray("complex", new int[]{124, 999, 33256}).build()).build(),
                "{\"nested\": {\"complex\": [I;124,999,33256]}}");

        assertInvalidArg(arg, "string");
        assertInvalidArg(arg, "\"string\"");
        assertInvalidArg(arg, "44");
        assertInvalidArg(arg, "[I;11,49,33]");
    }

    @Test
    public void testArgumentNbtTag() {
        var arg = ArgumentType.NBT("nbt");
        assertArg(arg, StringBinaryTag.stringBinaryTag("string"), "string");
        assertArg(arg, StringBinaryTag.stringBinaryTag("string"), "\"string\"");
        assertArg(arg, IntBinaryTag.intBinaryTag(44), "44");
        assertArg(arg, IntArrayBinaryTag.intArrayBinaryTag(11, 49, 33), "[I;11,49,33]");
        assertArg(arg, CompoundBinaryTag.builder().putLongArray("long_array", new long[]{12, 49, 119}).build(),
                "{\"long_array\":[L;12L,49L,119L]}");

        assertInvalidArg(arg, "\"unbalanced string");
        assertInvalidArg(arg, "dd}");
        assertInvalidArg(arg, "{unquoted: string)}");
        assertInvalidArg(arg, "{\"array\": [D;123L,5L]}");
    }

    @Test
    public void testArgumentResource() {
        var arg = ArgumentType.Resource("resource", "minecraft:block");
        assertArg(arg, "minecraft:resource_example", "minecraft:resource_example");
        assertInvalidArg(arg, "minecraft:invalid resource");
    }

    @Test
    public void testArgumentResourceLocation() {
        var arg = ArgumentType.ResourceLocation("resource_location");
        assertArg(arg, "minecraft:resource_location_example", "minecraft:resource_location_example");
        assertInvalidArg(arg, "minecraft:invalid resource location");
        //assertInvalidArg(arg, "minecraft:");
    }

    @Test
    public void testArgumentResourceOrTag() {
        var arg = ArgumentType.ResourceOrTag("resource_or_tag", "data/minecraft/tags/blocks");
        assertArg(arg, "minecraft:resource_or_tag_example", "minecraft:resource_or_tag_example");
        assertInvalidArg(arg, "minecraft:invalid resource or tag");
    }

    @Test
    public void testArgumentTime() {
        var arg = ArgumentType.Time("time");
        assertArg(arg, Duration.of(20, TimeUnit.SERVER_TICK), "20");
        assertArg(arg, Duration.of(40, TimeUnit.SERVER_TICK), "40t");
        assertArg(arg, Duration.of(60, TimeUnit.SECOND), "60s");
        assertArg(arg, Duration.of(80, TimeUnit.DAY), "80d");

        assertInvalidArg(arg, "100x");
        assertInvalidArg(arg, "2147483648t");
    }

    @Test
    public void testArgumentUUID() {
        var arg = ArgumentType.UUID("uuid");
        assertInvalidArg(arg, "invalid_uuid");
        assertArg(arg, UUID.fromString("10515090-26f2-49fa-b2ba-9594d4d0451f"), "10515090-26f2-49fa-b2ba-9594d4d0451f");
    }

    @Test
    public void testArgumentDouble() {
        var arg = ArgumentType.Double("double");
        assertArg(arg, 2564d, "2564");
        assertArg(arg, -591.981d, "-591.981");
        assertInvalidArg(arg, "-5.5.52");
        assertInvalidArg(arg, "++2.99");
    }

    @Test
    public void testArgumentFloat() {
        var arg = ArgumentType.Float("float");
        assertArg(arg, 2564f, "2564");
        assertArg(arg, -591.981f, "-591.981");
        assertInvalidArg(arg, "-5.5.52");
        assertInvalidArg(arg, "++2.99");
    }

    @Test
    public void testArgumentInteger() {
        var arg = ArgumentType.Integer("integer");
        assertArg(arg, 2564, "2564");
        assertInvalidArg(arg, "256.4");
        assertInvalidArg(arg, "2147483648");
    }

    @Test
    public void testArgumentLong() {
        var arg = ArgumentType.Long("long");
        assertArg(arg, 2564l, "2564");
        assertInvalidArg(arg, "256.4");
        assertInvalidArg(arg, "9223372036854775808");
    }

    @Test
    public void testArgumentRelativeBlockPosition() {
        var arg = ArgumentType.RelativeBlockPosition("relative_block_position");
        var vec = new Vec(-3, 14, 255);

        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, false, false), "-3 14 +255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.RELATIVE, true, false, false), "~-3 14 +255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, true, false), "-3 ~14 +255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, false, true), "-3 14 ~+255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.RELATIVE, true, true, true), "~-3 ~14 ~+255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.LOCAL, true, true, true), "^-3 ^14 ^+255");

        assertInvalidArg(arg, "-3.50 14 +255");
        assertInvalidArg(arg, "-3 14.25 +255");
        assertInvalidArg(arg, "-3 14 +255.75");
        assertInvalidArg(arg, "-3 14 +-255");
        assertInvalidArg(arg, "-3 text -255");
        assertInvalidArg(arg, "-3 14 ~~+255");
        assertInvalidArg(arg, "^-3 ~14 ^+255");
        assertInvalidArg(arg, "^-3 14 ^+255");
        assertInvalidArg(arg, "1 2");
        assertInvalidArg(arg, "1 2 3 4");
    }

    @Test
    public void testArgumentRelativeVec2() {
        var arg = ArgumentType.RelativeVec2("relative_vec_2");
        var vec = new Vec(-3, 14.25);

        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, false, false), "-3 14.25");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.RELATIVE, true, false, false), "~-3 14.25");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, false, true), "-3 ~14.25");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, false, true), "-3 ~14.25");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.RELATIVE, true, false, true), "~-3 ~14.25");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.LOCAL, true, false, true), "^-3 ^14.25");

        assertInvalidArg(arg, "-3 +-14");
        assertInvalidArg(arg, "-3 text");
        assertInvalidArg(arg, "~~-3 14");
        assertInvalidArg(arg, "^-3 ~14");
        assertInvalidArg(arg, "^-3 14");
        assertInvalidArg(arg, "1");
        assertInvalidArg(arg, "1 2 3");
    }

    @Test
    public void testArgumentRelativeVec3() {
        var arg = ArgumentType.RelativeVec3("relative_vec_3");
        var vec = new Vec(-3, 14.25, 255);

        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, false, false), "-3 14.25 +255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.RELATIVE, true, false, false), "~-3 14.25 +255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, true, false), "-3 ~14.25 +255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.ABSOLUTE, false, false, true), "-3 14.25 ~+255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.RELATIVE, true, true, true), "~-3 ~14.25 ~+255");
        assertArg(arg, new RelativeVec(vec, RelativeVec.CoordinateType.LOCAL, true, true, true), "^-3 ^14.25 ^+255");

        assertInvalidArg(arg, "-3 14 +-255");
        assertInvalidArg(arg, "-3 text -255");
        assertInvalidArg(arg, "-3 14 ~~+255");
        assertInvalidArg(arg, "^-3 ~14 ^+255");
        assertInvalidArg(arg, "^-3 14 ^+255");
        assertInvalidArg(arg, "1 2");
        assertInvalidArg(arg, "1 2 3 4");
    }

    @Test
    public void testArgumentBoolean() {
        var arg = ArgumentType.Boolean("boolean");
        assertArg(arg, true, "true");
        assertArg(arg, false, "false");
        assertInvalidArg(arg, "invalid_boolean");
    }

    @Test
    public void testArgumentEnum() {
        enum ExampleEnum {FIRST, SECOND, Third, fourth}

        var arg = ArgumentType.Enum("enum", ExampleEnum.class);

        arg.setFormat(ArgumentEnum.Format.DEFAULT);
        assertArg(arg, ExampleEnum.FIRST, "FIRST");
        assertArg(arg, ExampleEnum.SECOND, "SECOND");
        assertArg(arg, ExampleEnum.Third, "Third");
        assertArg(arg, ExampleEnum.fourth, "fourth");
        assertInvalidArg(arg, "invalid argument");

        arg.setFormat(ArgumentEnum.Format.UPPER_CASED);
        assertArg(arg, ExampleEnum.FIRST, "FIRST");
        assertArg(arg, ExampleEnum.SECOND, "SECOND");
        assertInvalidArg(arg, "Third");
        assertInvalidArg(arg, "fourth");
        assertInvalidArg(arg, "invalid argument");

        arg.setFormat(ArgumentEnum.Format.LOWER_CASED);
        assertInvalidArg(arg, "FIRST");
        assertInvalidArg(arg, "SECOND");
        assertInvalidArg(arg, "Third");
        assertArg(arg, ExampleEnum.fourth, "fourth");
        assertInvalidArg(arg, "invalid argument");
    }

    @Test
    public void testArgumentGroup() {
        var arg = ArgumentType.Group("group", ArgumentType.Integer("integer"), ArgumentType.String("string"), ArgumentType.Double("double"));

        // Test normal input
        var context1 = arg.parse(new ServerSender(), "1234 1234 1234");
        assertEquals(1234, context1.<Integer>get("integer"));
        assertEquals("1234", context1.<String>get("string"));
        assertEquals(1234.0, context1.<Double>get("double"));

        // Test different input + trailing spaces
        var context2 = arg.parse(new ServerSender(), "1234 abcd 1234.5678   ");
        assertEquals(1234, context2.<Integer>get("integer"));
        assertEquals("abcd", context2.<String>get("string"));
        assertEquals(1234.5678, context2.<Double>get("double"));

        assertInvalidArg(arg, "");
        assertInvalidArg(arg, "");
        assertInvalidArg(arg, "");
        assertInvalidArg(arg, "1234.5678 1234 1234.5678");
        assertInvalidArg(arg, "1234 1234 abcd");
        assertInvalidArg(arg, "1234 1234 ");
        assertInvalidArg(arg, "1234");
        assertInvalidArg(arg, "1234 abcd 1234.5678 extra");
    }

    @Test
    public void testArgumentLiteral() {
        var arg = ArgumentType.Literal("literal");
        assertArg(arg, "literal", "literal");
        assertInvalidArg(arg, "not_literal");
        assertInvalidArg(arg, "");
    }

    @Test
    public void testArgumentLoop() {
        var arg = ArgumentType.Loop("loop", ArgumentType.String("string"), ArgumentType.String("string2").map(s -> {
            throw new IllegalArgumentException("This argument should never be triggered");
        }));

        assertArg(arg, List.of("a", "b", "c"), "a b c");
        assertArg(arg, List.of("a", "b"), "a b");
    }

    @Test
    public void testArgumentString() {
        var arg = ArgumentType.String("string");
        assertArg(arg, "text", "text");
        assertArg(arg, "more text", "\"more text\"");
        assertArg(arg, "more text, but with \"escaped\" quotes", "\"more text, but with \\\"escaped\\\" quotes\"");
        assertInvalidArg(arg, "\"unclosed quotes");
        assertInvalidArg(arg, "\"unescaped \" quotes\"");
    }

    @Test
    public void testArgumentStringArray() {
        var arg = ArgumentType.StringArray("string_array");
        assertArrayArg(arg, new String[]{"example", "text"}, "example text");
        assertArrayArg(arg, new String[]{"some", "more", "placeholder", "text"}, "some more placeholder text");
        assertArrayArg(arg, new String[]{""}, "");
        assertArrayArg(arg, new String[0], " ");
        assertArrayArg(arg, new String[0], "         ");
    }

    @Test
    public void testArgumentWord() {
        var arg = ArgumentType.Word("word").from("word1", "word2", "word3");

        assertArg(arg, "word1", "word1");
        assertArg(arg, "word2", "word2");
        assertArg(arg, "word3", "word3");

        assertInvalidArg(arg, "word");
        assertInvalidArg(arg, "word4");
    }

    @Test
    public void testArgumentTransformWithSender() {
        var serverSender = new ServerSender();

        var arg = ArgumentType.Word("word").from("word1", "word2", "word3")
                .map((sender, s) -> {
                    assertEquals(serverSender, sender);
                    return s;
                });

        assertEquals("word1", arg.parse(serverSender, "word1"));
    }

    private static <T> void assertArg(Argument<T> arg, T expected, String input) {
        assertEquals(expected, arg.parse(new ServerSender(), input));
    }

    private static <T> void assertArrayArg(Argument<T[]> arg, T[] expected, String input) {
        assertArrayEquals(expected, arg.parse(new ServerSender(), input));
    }

    private static <T> void assertValidArg(Argument<T> arg, String input) {
        assertDoesNotThrow(() -> arg.parse(new ServerSender(), input));
    }

    private static <T> void assertInvalidArg(Argument<T> arg, String input) {
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(new ServerSender(), input));
    }
}
