package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceIDTest {

    @Test
    void init() {
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
    void equals() {
        var namespace = NamespaceID.from("minecraft:any");
        assertEquals(namespace, NamespaceID.from("minecraft:any"));
        assertNotEquals(namespace, NamespaceID.from("minecraft:any2"));
        assertEquals(namespace, Key.key("minecraft:any"));
    }

    @Test
    void hashCodeConsistentWithEquals() {
        var namespace = NamespaceID.from("minecraft:any");
        var key = Key.key("minecraft:any");

        assertEquals(namespace, key);
        assertEquals(namespace.hashCode(), key.hashCode());
    }

    @Test
    void atMostOneColon() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:block:wool"));
    }

    @Test
    void noSlashInDomain() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft/java_edition:any"));
    }

    @Test
    void noDotInDomain() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft.java:game"));
    }

    @Test
    void noUppercase() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("Minecraft:any"));
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:Any"));
    }

    @Test
    void noSpace() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:a n y"));
    }

    @Test
    void onlyLatinLowercase() {
        assertThrows(AssertionError.class, () -> NamespaceID.from("Minecraft:voilà"));
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:où_ça"));
        assertThrows(AssertionError.class, () -> NamespaceID.from("minecraft:schrödingers_var"));
    }

    @Test
    void numbersAllowed() {
        NamespaceID.from("0xc1:468786471");
    }

    @Test
    void dotAllowedInPath() {
        NamespaceID.from("minecraft:ambient.cave");
    }

    @Test
    void slashAllowedInPath() {
        NamespaceID.from("minecraft:textures/blocks/dirt.png");
    }
}
