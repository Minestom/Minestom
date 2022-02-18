package net.minestom.server.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMapColors {

    @Test
    public void closestColor() {
        assertEquals(MapColors.closestColor(0x539D87FF).getBaseColor(), MapColors.ICE);
    }

    @Test
    public void closestColorNoAlpha() {
        assertEquals(MapColors.closestColor(0xFFFFFF00).getBaseColor(), MapColors.NONE);
    }

}
