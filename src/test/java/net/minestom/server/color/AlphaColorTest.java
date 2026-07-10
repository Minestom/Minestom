package net.minestom.server.color;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.codec.Transcoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlphaColorTest {

    @Test
    public void alphaColorTest() {
        AlphaColor color = new AlphaColor(0x11, 0x22, 0x33, 0x44);
        assertEquals(0x11223344, color.asARGB());
        assertEquals(0x22334411, color.asRGBA());

        String hexString = "#AABBCCDD";
        assertEquals(
                new AlphaColor(0xDDAABBCC),
                AlphaColor.fromRGBAHexString(hexString)
        );
        assertEquals(
                new AlphaColor(0xAABBCCDD),
                AlphaColor.fromARGBHexString(hexString)
        );
    }

    @Test
    public void codecTest() {
        AlphaColor testColor = new AlphaColor(0x01, 0x23, 0x45, 0x67);
        BinaryTag elementARGB = AlphaColor.ARGB_STRING_CODEC.encode(Transcoder.NBT, testColor).orElseThrow();
        BinaryTag elementRGBA = AlphaColor.RGBA_STRING_CODEC.encode(Transcoder.NBT, testColor).orElseThrow();
        assertEquals(StringBinaryTag.stringBinaryTag("#01234567"), elementARGB);
        assertEquals(StringBinaryTag.stringBinaryTag("#23456701"), elementRGBA);
    }
}
