package net.minestom.server.tag;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

public class TagRecordTest {

    @Test
    public void basic() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Structure("vec", Vec.class);
        var vec = new Vec(1, 2, 3);
        assertNull(handler.getTag(tag));
        handler.setTag(tag, vec);
        assertEquals(vec, handler.getTag(tag));
    }

    @Test
    public void fromNBT() {
        var vecCompound = NBT.Compound(Map.of(
                "x", NBT.Double(1),
                "y", NBT.Double(2),
                "z", NBT.Double(3)));
        var handler = TagHandler.fromCompound(NBT.Compound(Map.of("vec", vecCompound)));
        var tag = Tag.Structure("vec", Vec.class);
        assertEquals(new Vec(1, 2, 3), handler.getTag(tag));
    }

    @Test
    public void fromNBTView() {
        var handler = TagHandler.fromCompound(NBT.Compound(Map.of(
                "x", NBT.Double(1),
                "y", NBT.Double(2),
                "z", NBT.Double(3))));
        var tag = Tag.View(Vec.class);
        assertEquals(new Vec(1, 2, 3), handler.getTag(tag));
    }

    @Test
    public void basicSerializer() {
        var handler = TagHandler.newHandler();
        var serializer = TagRecord.serializer(Vec.class);
        serializer.write(handler, new Vec(1, 2, 3));
        assertEquals(new Vec(1, 2, 3), serializer.read(handler));
    }

    @Test
    public void basicSnbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Structure("vec", Vec.class);
        var vec = new Vec(1, 2, 3);
        handler.setTag(tag, vec);
        assertEqualsSNBT("""
                {
                  "vec": {
                    "x":1D,
                    "y":2D,
                    "z":3D
                  }
                }
                """, handler.asCompound());
        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void nbtSerializer() {
        record CompoundRecord(NBTCompound compound) {
        }
        var test = new CompoundRecord(NBT.Compound(Map.of("key", NBT.String("value"))));
        var handler = TagHandler.newHandler();
        var serializer = TagRecord.serializer(CompoundRecord.class);
        serializer.write(handler, test);
        assertEquals(test, serializer.read(handler));
    }

    @Test
    public void unsupportedList() {
        record Test(List<Object> list) {
        }
        assertThrows(IllegalArgumentException.class, () -> Tag.Structure("test", Test.class));
    }

    @Test
    public void unsupportedArray() {
        record Test(Object[] array) {
        }
        assertThrows(IllegalArgumentException.class, () -> Tag.Structure("test", Test.class));
    }

    @Test
    public void forceRecord() {
        assertThrows(Throwable.class, () -> Tag.Structure("entity", Class.class.cast(Entity.class)));
    }

    @Test
    public void invalidItem() {
        // ItemStack cannot become a record due to `ItemStack#toItemNBT` being serialized differently, and independently of
        // the item record components
        assertThrows(Throwable.class, () -> Tag.Structure("item", Class.class.cast(ItemStack.class)));
    }
}
