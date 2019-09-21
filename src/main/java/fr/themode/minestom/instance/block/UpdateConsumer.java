package fr.themode.minestom.instance.block;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.utils.BlockPosition;

@FunctionalInterface
public interface UpdateConsumer {
    void update(Instance instance, BlockPosition blockPosition, Data data);
}
