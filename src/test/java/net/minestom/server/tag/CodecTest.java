package net.minestom.server.tag;

import com.google.gson.JsonElement;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodecTest {

    @Test
    public void testByteTag() {
        testItemStack(Tag.Byte("test"), (byte) 1);
    }

    @Test
    public void testBooleanTag() {
        testItemStack(Tag.Boolean("test"), true);
    }

    @Test
    public void testShortTag() {
        testItemStack(Tag.Short("test"), (short) 1);
    }

    @Test
    public void testIntTag() {
        testItemStack(Tag.Integer("test"), 1);
    }

    @Test
    public void testLongTag() {
        testItemStack(Tag.Long("test"), 1L);
    }

    @Test
    public void testFloatTag() {
        testItemStack(Tag.Float("test"), 1.0f);
    }

    @Test
    public void testDoubleTag() {
        testItemStack(Tag.Double("test"), 1.0);
    }

    @Test
    public void testStringTag() {
        testItemStack(Tag.String("test"), "string");
    }

    @Test
    public void testUUID() {
        testItemStack(Tag.UUID("test"), UUID.fromString("0000000-0000-0000-0000-000000000000"));
    }

    @Test
    public void testItemStack() {
        testItemStack(Tag.ItemStack("test"), ItemStack.of(Material.STICK));
    }

    @Test
    public void testComponent() {
        testItemStack(Tag.Component("test"), Component.text("component"));
    }

    @Test
    public void testNBT() {
        testItemStack(Tag.NBT("test"), CompoundBinaryTag.builder().putString("test", "value").build());
    }

    private static <T> void testItemStack(Tag<T> tag, T value) {
        final CompoundBinaryTag.Builder nbtBuilder = CompoundBinaryTag.builder();
        tag.write(nbtBuilder, value);
        final CompoundBinaryTag nbt = nbtBuilder.build();
        final JsonElement json = Codec.NBT_COMPOUND.encode(Transcoder.JSON, nbt).orElseThrow();
        System.out.println(json.toString());
        final CompoundBinaryTag decoded = Codec.NBT_COMPOUND.decode(Transcoder.JSON, json).orElseThrow();
        assertEquals(nbt, decoded);

        final CompoundBinaryTag.Builder nbtListBuilder = CompoundBinaryTag.builder();
        tag.list().write(nbtListBuilder, List.of(value, value));
        final CompoundBinaryTag tagList = nbtListBuilder.build();
        final JsonElement jsonList = Codec.NBT_COMPOUND.encode(Transcoder.JSON, tagList).orElseThrow();
        final CompoundBinaryTag decodedList = Codec.NBT_COMPOUND.decode(Transcoder.JSON, jsonList).orElseThrow();
        assertEquals(tagList, decodedList);
    }

}
