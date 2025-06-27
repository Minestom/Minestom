package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
<<<<<<< HEAD
=======
import net.minestom.server.instance.block.BlockChange;
>>>>>>> cc02c79fb (Cleanup)
import org.jetbrains.annotations.NotNull;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public @NotNull Key getKey() {
        return Key.key("minestom", "test");
    }

    @Override
<<<<<<< HEAD
    public void onPlace(@NotNull Placement placement) {
        System.out.println(placement);
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        System.out.println(destroy);
=======
    public @NotNull Block onPlace(@NotNull BlockChange mutation) {
        return Block.DIAMOND_BLOCK;
    }

    @Override
    public @NotNull Block onDestroy(@NotNull BlockChange mutation) {
        return Block.EMERALD_BLOCK;
>>>>>>> cc02c79fb (Cleanup)
    }
}
