package net.minestom.server.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.math.FloatRange;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.time.TimeUnit;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentTest {

    @Test
    public void testArgumentEnchantment() {
        var arg = ArgumentType.Enchantment("enchantment");

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:invalid_enchantment"));
        assertNotEquals(Enchantment.RESPIRATION, arg.parse(Enchantment.SWEEPING.namespace().asString()));
        assertEquals(Enchantment.MENDING, arg.parse(Enchantment.MENDING.namespace().asString()));

        assertEquals("Enchantment<enchantment>", arg.toString());
    }

    @Test
    public void testArgumentEntityType() {
        var arg = ArgumentType.EntityType("entity_type");

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:invalid_entity_type"));
        assertNotEquals(EntityType.ARMOR_STAND, arg.parse(EntityType.HUSK.namespace().asString()));
        assertEquals(EntityType.PLAYER, arg.parse(EntityType.PLAYER.namespace().asString()));


        assertEquals("EntityType<entity_type>", arg.toString());
    }

    @Test
    public void testArgumentParticle() {
        var arg = ArgumentType.Particle("particle");

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:invalid_particle"));
        assertNotEquals(Particle.BLOCK, arg.parse(Particle.CAMPFIRE_SIGNAL_SMOKE.namespace().asString()));
        assertEquals(Particle.TOTEM_OF_UNDYING, arg.parse(Particle.TOTEM_OF_UNDYING.namespace().asString()));

        assertEquals("Particle<particle>", arg.toString());
    }

    @Test
    public void testArgumentPotionEffect() {
        var arg = ArgumentType.Potion("potion");

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:invalid_potion"));
        assertNotEquals(PotionEffect.SPEED, arg.parse(PotionEffect.JUMP_BOOST.namespace().asString()));
        assertEquals(PotionEffect.INSTANT_DAMAGE, arg.parse(PotionEffect.INSTANT_DAMAGE.namespace().asString()));

        assertEquals("Potion<potion>", arg.toString());
    }

    @Test
    public void testArgumentBlockState() {
        var arg = ArgumentType.BlockState("block_state");

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:invalid_block[invalid_property=invalid_key]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:stone[invalid_property=invalid_key]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:kelp[age=invalid_key]"));
        assertEquals(Block.COBBLESTONE, arg.parse("minecraft:cobblestone"));
        assertEquals(Block.KELP.withProperty("age", "14"), arg.parse("minecraft:kelp[age=14]"));
        assertNotEquals(Block.KELP.withProperty("age", "15"), arg.parse("minecraft:kelp[age=14]"));
        assertNotEquals(Block.ATTACHED_MELON_STEM, arg.parse("minecraft:cobblestone"));

        assertEquals("BlockState<block_state>", arg.toString());
    }

    @Test
    public void testArgumentColor() {
        var arg = ArgumentType.Color("color");

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("invalid_color"));
        assertNotEquals(Style.style(NamedTextColor.AQUA), arg.parse("blue"));
        assertEquals(Style.style(NamedTextColor.DARK_PURPLE), arg.parse("dark_purple"));
        assertEquals(Style.empty(), arg.parse("reset"));

        assertEquals("Color<color>", arg.toString());
    }

    @Test
    public void testArgumentComponent() {
        var arg = ArgumentType.Component("component");

        var component1 = Component.text("Example text", NamedTextColor.DARK_AQUA);
        var component2 = Component.text("Other example text", Style.style(TextDecoration.OBFUSCATED));

        var json1 = GsonComponentSerializer.gson().serialize(component1);
        var json2 = GsonComponentSerializer.gson().serialize(component2);

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("invalid component"));
        assertNotEquals(component1, arg.parse(json2));
        assertEquals(component1, arg.parse(json1));
        assertEquals("Other example text", PlainTextComponentSerializer.plainText().serialize(arg.parse(json2)));

        assertEquals("Component<component>", arg.toString());
    }

    @Test
    public void testArgumentEntity() {
        var arg = ArgumentType.Entity("entity");

        assertDoesNotThrow(() -> arg.parse("@a"));
        assertDoesNotThrow(() -> arg.parse("@p"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@x"));

        assertDoesNotThrow(() -> arg.parse("@e[type=sheep]"));
        assertDoesNotThrow(() -> arg.parse("@e[type=!cow]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[type=invalid_entity]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[type=!invalid_entity_two]"));

        assertDoesNotThrow(() -> arg.parse("@e[gamemode=creative]"));
        assertDoesNotThrow(() -> arg.parse("@e[gamemode=!survival]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[gamemode=invalid_gamemode]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[gamemode=!invalid_gamemode_2]"));

        assertDoesNotThrow(() -> arg.parse("@e[limit=500]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[limit=-500]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[limit=invalid_integer]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[limit=2147483648]"));

        assertDoesNotThrow(() -> arg.parse("@e[sort=nearest]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[sort=invalid_sort]"));

        assertDoesNotThrow(() -> arg.parse("@e[level=55]"));
        assertDoesNotThrow(() -> arg.parse("@e[level=100..500]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[level=20-50]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[level=2147483648]"));

        assertDoesNotThrow(() -> arg.parse("@e[distance=500]"));
        assertDoesNotThrow(() -> arg.parse("@e[distance=50..150]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[distance=-500-500]"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("@e[distance=2147483648]"));

        assertEquals("Entities<entity>", arg.toString());
    }

    @Test
    public void testArgumentFloatRange() {
        var arg = ArgumentType.FloatRange("float_range");

        assertEquals(new FloatRange(0f, 50f), arg.parse("0..50"));
        assertEquals(new FloatRange(0f, 0f), arg.parse("0..0"));
        assertEquals(new FloatRange(-50f, 0f), arg.parse("-50..0"));
        assertEquals(new FloatRange(-Float.MAX_VALUE, 50f), arg.parse("..50"));
        assertEquals(new FloatRange(0f, Float.MAX_VALUE), arg.parse("0.."));
        assertEquals(new FloatRange(-Float.MAX_VALUE, Float.MAX_VALUE), arg.parse("-3.4028235E38..3.4028235E38"));
        assertEquals(new FloatRange(0.5f, 24f), arg.parse("0.5..24"));
        assertEquals(new FloatRange(12f, 45.6f), arg.parse("12..45.6"));

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(".."));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("0..50.."));

        assertEquals("FloatRange<float_range>", arg.toString());
    }

    @Test
    public void testArgumentIntRange() {
        var arg = ArgumentType.IntRange("int_range");

        assertEquals(new IntRange(0, 50), arg.parse("0..50"));
        assertEquals(new IntRange(0, 0), arg.parse("0..0"));
        assertEquals(new IntRange(-50, 0), arg.parse("-50..0"));
        assertEquals(new IntRange(Integer.MIN_VALUE, 50), arg.parse("..50"));
        assertEquals(new IntRange(0, Integer.MAX_VALUE), arg.parse("0.."));
        assertEquals(new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE), arg.parse("-2147483648..2147483647"));

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(".."));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("-2147483649..2147483647"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("-2147483648..2147483648"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("0..50.."));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("0.5..24"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("12..45.6"));

        assertEquals("IntRange<int_range>", arg.toString());
    }

    @Test
    public void testArgumentItemStack() {
        var arg = ArgumentType.ItemStack("item_stack");

        assertEquals(ItemStack.AIR, arg.parse("air"));
        assertEquals(ItemStack.of(Material.GLASS_PANE).withTag(Tag.String("tag"), "value"), arg.parse("glass_pane{tag:value}"));

        assertEquals("ItemStack<item_stack>", arg.toString());
    }

    @Test
    public void testArgumentNbtCompoundTag() {
        var arg = ArgumentType.NbtCompound("nbt_compound");

        // TODO: Finish

        assertEquals("NbtCompound<nbt_compound>", arg.toString());
    }

    @Test
    public void testArgumentNbtTag() {
        var arg = ArgumentType.NBT("nbt");

        // TODO: Finish

        assertEquals("NBT<nbt>", arg.toString());
    }

    @Test
    public void testArgumentResourceLocation() {
        var arg = ArgumentType.ResourceLocation("resource_location");

        assertEquals("minecraft:resource_location_example", arg.parse("minecraft:resource_location_example"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("minecraft:invalid resource location"));

        assertEquals("ResourceLocation<resource_location>", arg.toString());
    }

    @Test
    public void testArgumentTime() {
        var arg = ArgumentType.Time("time");

        // TODO: Fix argument durations - d=24000t, s=20t, t=1t
        assertEquals(Duration.of(20, TimeUnit.SERVER_TICK), arg.parse("20"));
        assertEquals(Duration.of(40, TimeUnit.SERVER_TICK), arg.parse("40t"));
        assertEquals(Duration.of(60, TimeUnit.SECOND), arg.parse("60s"));
        assertEquals(Duration.of(80, TimeUnit.DAY), arg.parse("80d"));

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("100x"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("2147483648t"));

        assertEquals("Time<time>", arg.toString());
    }

    @Test
    public void testArgumentUUID() {
        var arg = ArgumentType.UUID("uuid");

        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("invalid_uuid"));
        assertEquals(UUID.fromString("10515090-26f2-49fa-b2ba-9594d4d0451f"), arg.parse("10515090-26f2-49fa-b2ba-9594d4d0451f"));

        assertEquals("UUID<uuid>", arg.toString());
    }

    @Test
    public void testArgumentDouble() {
        var arg = ArgumentType.Double("double");

        assertEquals(2564d, arg.parse("2564"));
        assertEquals(-591.981d, arg.parse("-591.981"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("-5.5.52"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("++2.99"));

        assertEquals("Double<double>", arg.toString());
    }

    @Test
    public void testArgumentFloat() {
        var arg = ArgumentType.Float("float");

        assertEquals(2564f, arg.parse("2564"));
        assertEquals(-591.981f, arg.parse("-591.981"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("-5.5.52"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("++2.99"));

        assertEquals("Float<float>", arg.toString());
    }

    @Test
    public void testArgumentInteger() {
        var arg = ArgumentType.Integer("integer");

        assertEquals(2564, arg.parse("2564"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("256.4"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("2147483648"));

        assertEquals("Integer<integer>", arg.toString());
    }

    @Test
    public void testArgumentLong() {
        var arg = ArgumentType.Long("long");

        assertEquals(2564, arg.parse("2564"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("256.4"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("9223372036854775808"));

        assertEquals("Long<long>", arg.toString());
    }

    @Test
    public void testArgumentRelativeBlockPosition() {
        var arg = ArgumentType.RelativeBlockPosition("relative_block_position");

        // TODO: Complete

        assertEquals("RelativeBlockPosition<relative_block_position>", arg.toString());
    }

    @Test
    public void testArgumentRelativeVec2() {
        var arg = ArgumentType.RelativeVec2("relative_vec_2");

        // TODO: Complete

        assertEquals("RelativeVec2<relative_vec_2>", arg.toString());
    }

    @Test
    public void testArgumentRelativeVec3() {
        var arg = ArgumentType.RelativeVec3("relative_vec_3");

        // TODO: Complete

        assertEquals("RelativeVec3<relative_vec_3>", arg.toString());
    }

    @Test
    public void testArgumentBoolean() {
        var arg = ArgumentType.Boolean("boolean");

        assertEquals(true, arg.parse("true"));
        assertNotEquals(false, arg.parse("true"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("invalid_boolean"));

        assertEquals("Boolean<boolean>", arg.toString());
    }

    @Test
    public void testArgumentCommand() {
        var arg = ArgumentType.Command("command");

        // TODO: Complete

        assertEquals("Command<command>", arg.toString());
    }

    @Test
    public void testArgumentEnum() {
        var arg = ArgumentType.Enum("enum", null); // TODO: Class

        // TODO: Complete

        assertEquals("Enum<enum>", arg.toString());
    }

    @Test
    public void testArgumentGroup() {
        var arg = ArgumentType.Group("group", null); // TODO: Arguments

        // TODO: Complete

        assertEquals("Group<group>", arg.toString());
    }

    @Test
    public void testArgumentLiteral() {
        var arg = ArgumentType.Literal("literal");

        assertEquals("literal", arg.parse("literal"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("not_literal"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(""));

        assertEquals("Literal<literal>", arg.toString());
    }

    @Test
    public void testArgumentLoop() {
        var arg = ArgumentType.Loop("loop", null); // TODO: Arguments

        // TODO: Complete

        assertEquals("Loop<loop>", arg.toString());
    }

    @Test
    public void testArgumentString() {
        var arg = ArgumentType.String("string");

        // TODO: Complete

        assertEquals("String<string>", arg.toString());
    }

    @Test
    public void testArgumentStringArray() {
        var arg = ArgumentType.StringArray("string_array");

        assertArrayEquals(new String[]{"example", "text"}, arg.parse("example text"));
        assertArrayEquals(new String[]{"some", "more", "placeholder", "text"}, arg.parse("some more placeholder text"));
        assertArrayEquals(new String[]{""}, arg.parse(""));
        assertArrayEquals(new String[0], arg.parse(" "));
        assertArrayEquals(new String[0], arg.parse("         "));

        assertEquals("StringArray<string_array>", arg.toString());
    }

    @Test
    public void testArgumentWord() {
        var arg = ArgumentType.Word("word").from("word1", "word2", "word3");

        assertEquals("word1", arg.parse("word1"));
        assertEquals("word2", arg.parse("word2"));
        assertEquals("word3", arg.parse("word3"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("word"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse("word4"));

        assertEquals("Word<word>", arg.toString());
    }

}
