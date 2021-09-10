package net.minestom.server.instance.block;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class BlockHandlerBase implements BlockHandler {

    private final NamespaceID namespace;

    public BlockHandlerBase(String name) {
        namespace = NamespaceID.from(name);
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return namespace;
    }
}
