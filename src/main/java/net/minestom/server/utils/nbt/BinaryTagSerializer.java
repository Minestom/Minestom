package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface BinaryTagSerializer<T> {

    BinaryTagSerializer<Void> NOTHING = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Void value) {
            return EndBinaryTag.endBinaryTag();
        }

        @Override
        public @NotNull Void read(@NotNull BinaryTag tag) {
            return null;
        }
    };

    BinaryTagSerializer<Byte> BYTE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Byte value) {
            return ByteBinaryTag.byteBinaryTag(value);
        }

        @Override
        public @NotNull Byte read(@NotNull BinaryTag tag) {
            return tag instanceof ByteBinaryTag byteBinaryTag ? byteBinaryTag.value() : 0;
        }
    };

    BinaryTagSerializer<Boolean> BOOLEAN = BYTE.map(b -> b != 0, b -> (byte) (b ? 1 : 0));

    BinaryTagSerializer<Integer> INT = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Integer value) {
            return IntBinaryTag.intBinaryTag(value);
        }

        @Override
        public @NotNull Integer read(@NotNull BinaryTag tag) {
            return tag instanceof IntBinaryTag intBinaryTag ? intBinaryTag.value() : 0;
        }
    };

    BinaryTagSerializer<String> STRING = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull String value) {
            return StringBinaryTag.stringBinaryTag(value);
        }

        @Override
        public @NotNull String read(@NotNull BinaryTag tag) {
            return tag instanceof StringBinaryTag stringBinaryTag ? stringBinaryTag.value() : "";
        }
    };

    BinaryTagSerializer<CompoundBinaryTag> COMPOUND = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull CompoundBinaryTag value) {
            return value;
        }

        @Override
        public @NotNull CompoundBinaryTag read(@NotNull BinaryTag tag) {
            return tag instanceof CompoundBinaryTag compoundBinaryTag ? compoundBinaryTag : CompoundBinaryTag.empty();
        }
    };

    BinaryTagSerializer<Component> JSON_COMPONENT = STRING.map(
            s -> GsonComponentSerializer.gson().deserialize(s),
            c -> GsonComponentSerializer.gson().serialize(c)
    );
    BinaryTagSerializer<ItemStack> ITEM = COMPOUND.map(ItemStack::fromItemNBT, ItemStack::toItemNBT);

    @NotNull BinaryTag write(@NotNull T value);
    @NotNull T read(@NotNull BinaryTag tag);

    default <S> BinaryTagSerializer<S> map(@NotNull Function<T, S> to, @NotNull Function<S, T> from) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull S value) {
                return BinaryTagSerializer.this.write(from.apply(value));
            }

            @Override
            public @NotNull S read(@NotNull BinaryTag tag) {
                return to.apply(BinaryTagSerializer.this.read(tag));
            }
        };
    }

    default BinaryTagSerializer<List<T>> list() {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull List<T> value) {
                ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
                for (T t : value) builder.add(BinaryTagSerializer.this.write(t));
                return builder.build();
            }

            @Override
            public @NotNull List<T> read(@NotNull BinaryTag tag) {
                if (!(tag instanceof ListBinaryTag listBinaryTag)) return List.of();
                List<T> list = new ArrayList<>();
                for (BinaryTag element : listBinaryTag)
                    list.add(BinaryTagSerializer.this.read(element));
                return list;
            }
        };
    }
}
