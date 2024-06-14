package net.minestom.server.tag;

import net.minestom.server.item.firework.FireworkEffect;
import net.minestom.server.item.firework.FireworkEffectType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TagSerializerTest {
    @Test
    public void fromCompound(){
        var serializer = TagSerializer.fromCompound(FireworkEffect::fromCompound, FireworkEffect::asCompound);
        var effect = new FireworkEffect(false, false, FireworkEffectType.BURST, List.of(), List.of());
        TagHandler handler = TagHandler.newHandler();
        serializer.write(handler, effect);
        Assertions.assertEquals(effect, serializer.read(handler));
    }
}
