package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public Key getKey() {
        return Key.key("minestom", "test");
    }

    @Override
    public void onPlace(Placement placement) {
        System.out.println(placement);
    }

    @Override
    public void onDestroy(Destroy destroy) {
        System.out.println(destroy);
    }
}
