package misc;

import net.minestom.server.utils.NamespaceID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestNamespaceIDValidity {

    @Test
    public void noErrorWithCorrectFormat() {
        NamespaceID.from("minecraft:any");
    }

    @Test
    public void atMostOneColon() {
        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("minecraft:block:wool");
        });
    }

    @Test
    public void noSlashInDomain() {
        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("minecraft/java_edition:any");
        });
    }

    @Test
    public void noDotInDomain() {
        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("minecraft.java:game");
        });
    }

    @Test
    public void noUppercase() {
        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("Minecraft:any");
        });

        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("minecraft:Any");
        });
    }

    @Test
    public void noSpace() {
        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("minecraft:a n y");
        });
    }

    @Test
    public void onlyLatinLowercase() {
        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("Minecraft:voilà");
        });

        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("minecraft:où_ça");
        });

        assertThrows(AssertionError.class, () -> {
            NamespaceID.from("minecraft:schrödingers_var");
        });
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
