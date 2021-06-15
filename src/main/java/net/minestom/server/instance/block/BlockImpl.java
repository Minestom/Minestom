package net.minestom.server.instance.block;

import net.minestom.server.utils.NamespaceID;

import java.util.List;

@Deprecated
class BlockImpl {
    protected static Block create(NamespaceID namespaceID, short blockId, short minStateId, short maxStateId,
                                  short defaultStateId, List<BlockProperty<?>> properties) {
        return BlockRegistry.get(namespaceID.asString());
    }
}