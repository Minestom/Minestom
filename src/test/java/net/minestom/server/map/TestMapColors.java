package net.minestom.server.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMapColors {

    @Test
    public void closestColor() {
        assertEquals(MapColors.closestColor(0xFF539D87).getBaseColor(), MapColors.DIAMOND);
    }

    @Test
    public void closestColorNoAlpha() {
        assertEquals(MapColors.closestColor(0x00FFFFFF).getBaseColor(), MapColors.NONE);
    }

}
