package net.minestom.server.instance;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static net.minestom.server.utils.block.BlockUtils.parseProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockPropertiesTest {

    @Test
    public void empty() {
        assertEquals(Map.of(), parseProperties("[]"));
        assertEquals(Map.of(), parseProperties(""));
    }

    @Test
    public void noBrackets() {
        assertEquals(Map.of(), parseProperties("random test without brackets"));
        assertEquals(Map.of(), parseProperties("["));
        assertEquals(Map.of(), parseProperties("[end"));
        assertEquals(Map.of(), parseProperties("[random test without end bracket"));
        assertEquals(Map.of(), parseProperties("]"));
        assertEquals(Map.of(), parseProperties("start]"));
        assertEquals(Map.of(), parseProperties("random test without start bracket]"));
    }

    @Test
    public void spaces() {
        assertEquals(Map.of(), parseProperties("[    ]"));
    }

    @Test
    public void comma() {
        assertEquals(Map.of(), parseProperties("[  , , ,,,,  ]"));
    }

    @Test
    public void single() {
        assertEquals(Map.of("facing", "east"), parseProperties("[facing=east]"));
    }

    @Test
    public void doubleSpace() {
        assertEquals(Map.of("facing", "east", "key", "value"), parseProperties("[facing=east,key=value ]"));
        assertEquals(Map.of("facing", "east", "key", "value"), parseProperties("[ facing = east, key= value ]"));
    }

    @Test
    public void allLengths() {
        // Verify all length variations
        for (int i = 0; i < 13; i++) {
            StringBuilder properties = new StringBuilder("[");
            for (int j = 0; j < i; j++) {
                properties.append("key").append(j).append("=value").append(j);
                if (j != i - 1) properties.append(",");
            }
            properties.append("]");

            var map = parseProperties(properties.toString());
            assertEquals(i, map.size());
            for (int j = 0; j < i; j++) {
                assertEquals("value" + j, map.get("key" + j));
            }
        }
    }

    @Test
    public void corrupted() {
        final int size = 12;
        StringBuilder properties = new StringBuilder("[");
        for (int j = 0; j < size; j++) {
            properties.append("key").append(j).append("=value").append(j);
            if (j != size - 1) properties.append(",");
        }
        properties.append(", , ,]");

        var map = parseProperties(properties.toString());
        assertEquals(size, map.size());
        for (int j = 0; j < size; j++) {
            assertEquals("value" + j, map.get("key" + j));
        }
    }
}
