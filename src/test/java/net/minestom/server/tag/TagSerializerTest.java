package net.minestom.server.tag;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.component.FireworkExplosion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TagSerializerTest {
    @Test
    public void fromCompound(){
        var serializer = TagSerializer.fromCompound(FireworkExplosion.NBT_TYPE::read, explosion -> (CompoundBinaryTag) FireworkExplosion.NBT_TYPE.write(explosion));
        var effect = new FireworkExplosion(FireworkExplosion.Shape.BURST, List.of(), List.of(), false, false);
        TagHandler handler = TagHandler.newHandler();
        serializer.write(handler, effect);
        Assertions.assertEquals(effect, serializer.read(handler));
    }
}
