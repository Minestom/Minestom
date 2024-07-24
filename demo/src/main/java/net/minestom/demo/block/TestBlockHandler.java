package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public @NotNull Key getNamespaceId() {
        return Key.key("minestom", "test");
    }

    @Override
    public void onPlace(@NotNull Placement placement) {
        System.out.println(placement);
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        System.out.println(destroy);
    }
}
