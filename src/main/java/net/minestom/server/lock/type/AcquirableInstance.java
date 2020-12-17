package net.minestom.server.lock.type;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class AcquirableInstance extends AcquirableImpl<Instance> {
    public AcquirableInstance(@NotNull Instance value) {
        super(value);
    }
}
