package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NamespaceIDTest {

    @Test
    public void init() {
        var namespace = NamespaceID.from("minecraft:any");
        assertEquals("minecraft", namespace.domain());
        assertEquals("any", namespace.path());
        assertEquals("minecraft:any", namespace.toString());

        namespace = NamespaceID.from("stone");
        assertEquals("minecraft", namespace.domain());
        assertEquals("stone", namespace.path());
        assertEquals("minecraft:stone", namespace.toString());
    }

    @Test
    public void equals() {
        var namespace = NamespaceID.from("minecraft:any");
        assertEquals(namespace, NamespaceID.from("minecraft:any"));
        assertNotEquals(namespace, NamespaceID.from("minecraft:any2"));
        assertEquals(namespace, Key.key("minecraft:any"));
    }

    @Test
    public void atMostOneColon() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:block:wool"));
    }

    @Test
    public void noSlashInDomain() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft/java_edition:any"));
    }

    @Test
    public void noDotInDomain() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft.java:game"));
    }

    @Test
    public void noUppercase() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("Minecraft:any"));
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:Any"));
    }

    @Test
    public void noSpace() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:a n y"));
    }

    @Test
    public void onlyLatinLowercase() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("Minecraft:voilà"));
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:où_ça"));
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:schrödingers_var"));
    }

    @Test
    public void numbersAllowed() {
        NamespaceID.from("0xc1:468786471");
    }

    @Test
    public void dotAllowedInPath() {
        NamespaceID.from("minecraft:ambient.cave");
    }

    @Test
    public void slashAllowedInPath() {
        NamespaceID.from("minecraft:textures/blocks/dirt.png");
    }
}
