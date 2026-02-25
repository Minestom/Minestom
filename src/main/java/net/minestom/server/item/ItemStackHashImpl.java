package net.minestom.server.item;

import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//TODO(1.21.5) hashes of components should be cached. Vanilla does it on a per player basis, could also do it globally perhaps.
final class ItemStackHashImpl {

    public static ItemStack.Hash of(Transcoder<Integer> hashCoder, ItemStack itemStack) {
        if (itemStack.isAir()) return net.minestom.server.item.ItemStack.Hash.AIR;

        final Map<DataComponent<?>, Integer> addedComponents = new HashMap<>();
        final Set<DataComponent<?>> removedComponents = new HashSet<>();
        for (var entry : itemStack.componentPatch().entrySet()) {
            if (entry.value() != null) {
                addedComponents.put(entry.component(), ((DataComponent<Object>) entry.component()).encode(hashCoder, entry.value()).orElseThrow());
            } else {
                removedComponents.add(entry.component());
            }

        }
        return new ItemStackHashImpl.Item(
                itemStack.material(),
                itemStack.amount(),
                addedComponents,
                removedComponents
        );
    }

    public static final NetworkBuffer.Type<ItemStack.Hash> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, ItemStack.Hash value) {
            if (!(value instanceof Item item)) {
                buffer.write(NetworkBuffer.BOOLEAN, false);
                return;
            }

            buffer.write(NetworkBuffer.BOOLEAN, true);
            buffer.write(Item.NETWORK_TYPE, item);
        }

        @Override
        public ItemStack.Hash read(NetworkBuffer buffer) {
            if (!buffer.read(NetworkBuffer.BOOLEAN))
                return ItemStack.Hash.AIR;
            return buffer.read(Item.NETWORK_TYPE);
        }
    };

    record Air() implements ItemStack.Hash {
    }

    record Item(
            Material material,
            int amount,
            Map<DataComponent<?>, Integer> addedComponents,
            Set<DataComponent<?>> removedComponents
    ) implements ItemStack.Hash {
        private static final int MAX_COMPONENTS = 256;
        public static final NetworkBuffer.Type<Item> NETWORK_TYPE = NetworkBufferTemplate.template(
                Material.NETWORK_TYPE, Item::material,
                NetworkBuffer.VAR_INT, Item::amount,
                DataComponent.NETWORK_TYPE.mapValue(NetworkBuffer.INT, MAX_COMPONENTS), Item::addedComponents,
                DataComponent.NETWORK_TYPE.set(MAX_COMPONENTS), Item::removedComponents,
                Item::new);
    }
}
