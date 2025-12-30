package net.minestom.server.color;

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
}
