package net.minestom.demo.block;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public record TestBlockHandler(NamespaceID namespaceId) implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler(NamespaceID.from("minestom", "test"));

    @Override
    public void onPlace(@NotNull Placement placement) {
        System.out.println(placement);
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        System.out.println(destroy);
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        System.out.println(interaction);
        return true;
    }

    @Override
    public void onTouch(@NotNull Touch touch) {
        System.out.println(touch);
    }
}
