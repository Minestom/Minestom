package net.minestom.server.condition;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.codec.Codec;

public interface DataPredicate {
    record Noop(BinaryTag content) implements DataPredicate {

    }

    // TODO
    Codec<DataPredicate> NBT_TYPE = Codec.NBT.transform(Noop::new, value -> ((Noop) value).content);
}
