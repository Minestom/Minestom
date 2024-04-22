package net.minestom.demo.block;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from("minestom", "test");
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
