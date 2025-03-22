package net.minestom.server.utils.nbt;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>API Note: This class and associated types are currently considered an internal api. It is likely there will be
 * significant changes in the future, and there will not be backwards compatibility for this. Use at your own risk.</p>
 */
@ApiStatus.Internal
public interface BinaryTagSerializer<T> {

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

    interface Context {
        Context EMPTY = new Context() {
            @Override
            public @Nullable Registries registries() {
                return null;
            }

            @Override
            public boolean forClient() {
                return false;
            }
        };

        @Nullable Registries registries();

        boolean forClient();
    }

    record ContextWithRegistries(@NotNull Registries registries, boolean forClient) implements Context {

        public ContextWithRegistries(@NotNull Registries registries) {
            this(registries, false);
        }
    }

    default @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
        return write(value);
    }
    default @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
        return read(tag);
    }

    default @NotNull BinaryTag write(@NotNull T value) {
        return write(Context.EMPTY, value);
    }

    default @NotNull T read(@NotNull BinaryTag tag) {
        return read(Context.EMPTY, tag);
    }

    default <S> BinaryTagSerializer<S> map(@NotNull Function<T, S> to, @NotNull Function<S, T> from) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull S value) {
                return BinaryTagSerializer.this.write(context, from.apply(value));
            }

            @Override
            public @NotNull S read(@NotNull Context context, @NotNull BinaryTag tag) {
                return to.apply(BinaryTagSerializer.this.read(context, tag));
            }
        };
    }
}
