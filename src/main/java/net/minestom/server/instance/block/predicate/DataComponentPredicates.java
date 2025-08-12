package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.Unit;

import java.util.List;

// TODO: Pending pr #2732
public class DataComponentPredicates {
    public static final DataComponentPredicates EMPTY = new DataComponentPredicates();

    public static final NetworkBuffer.Type<DataComponentPredicates> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.UNIT.list(), DataComponentPredicates::exact,
            NetworkBuffer.UNIT.list(), DataComponentPredicates::partial,
            DataComponentPredicates::new);
    public static final Codec<DataComponentPredicates> CODEC = StructCodec.struct(new DataComponentPredicates());

    private DataComponentPredicates() {
    }

    private DataComponentPredicates(List<Unit> exact, List<Unit> partial) {
    }

    private List<Unit> exact() {
        return List.of();
    }

    private List<Unit> partial() {
        return List.of();
    }
}
