package net.minestom.server.instance.block;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;

@FunctionalInterface
public interface UpdateConsumer {
    void update(Instance instance, BlockPosition blockPosition, Data data);
}
