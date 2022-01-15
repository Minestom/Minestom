package regressions;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ItemMetaBuilderRegressions {

    private static class BasicMetaBuilder extends ItemMetaBuilder {
        @Override
        public @NotNull ItemMeta build() {
            return new ItemMeta(this) {};
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            // don't care
        }

        @Override
        public void handleCompound(@NotNull String key, @NotNull Consumer<@NotNull MutableNBTCompound> consumer) {
            super.handleCompound(key, consumer);
        }
    }

    @Test
    public void handleCompoundShouldUsePreviousValue() {
        BasicMetaBuilder builder = new BasicMetaBuilder();

        builder.handleCompound("test", nbt -> {
            nbt.setString("my_key", "AAA");
        });

        NBTCompound nbt = builder.build().toNBT().getCompound("test");

        assertTrue(nbt.contains("my_key"));
        assertEquals(1, nbt.getSize());
        assertTrue(nbt.get("my_key") instanceof NBTString);
        assertEquals("AAA", nbt.getString("my_key"));

        builder.handleCompound("test", n -> {
            n.setString("my_other_key", "BBB");
        });

        nbt = builder.build().toNBT().getCompound("test");

        assertTrue(nbt.contains("my_key"));
        assertTrue(nbt.contains("my_other_key"));
        assertEquals(2, nbt.getSize());
        assertTrue(nbt.get("my_key") instanceof NBTString);
        assertTrue(nbt.get("my_other_key") instanceof NBTString);
        assertEquals("AAA", nbt.getString("my_key"));
        assertEquals("BBB", nbt.getString("my_other_key"));
    }

    @Test
    public void clearingShouldRemoveData() {
        BasicMetaBuilder builder = new BasicMetaBuilder();

        builder.handleCompound("test", nbt -> {
            nbt.setString("my_key", "AAA");
        });

        NBTCompound nbt = builder.build().toNBT().getCompound("test");
        assertTrue(nbt.contains("my_key"));
        assertEquals(1, nbt.getSize());
        assertTrue(nbt.get("my_key") instanceof NBTString);
        assertEquals("AAA", nbt.getString("my_key"));

        builder.handleCompound("test", n -> {
            n.clear();
        });

        NBTCompound rootNBT = builder.build().toNBT();

        assertFalse(rootNBT.contains("test"));
        assertEquals(0, rootNBT.getSize());
    }
}
