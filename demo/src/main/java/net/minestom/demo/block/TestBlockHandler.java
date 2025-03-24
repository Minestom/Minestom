package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.event.instance.InstanceBlockChangeEvent;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class TestBlockHandler implements BlockHandler {
    public static final BlockHandler INSTANCE = new TestBlockHandler();

    @Override
    public @NotNull Key getKey() {
        return Key.key("minestom", "test");
    }

    @Override
    public void onBlockChange(@NotNull InstanceBlockChangeEvent event) {
        System.out.println(event);
    }
}