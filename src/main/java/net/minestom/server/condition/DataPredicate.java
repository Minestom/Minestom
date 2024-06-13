package net.minestom.server.condition;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public interface DataPredicate {
    record Noop(BinaryTag content) implements DataPredicate {

    }

    public static final BinaryTagSerializer<DataPredicate> NBT_TYPE = new BinaryTagSerializer<DataPredicate>() {
        @Override
        public @NotNull BinaryTag write(@NotNull DataPredicate value) {
            return ((Noop) value).content;
        }

        @Override
        public @NotNull DataPredicate read(@NotNull BinaryTag tag) {
            return new Noop(tag);
        }
    };
}
