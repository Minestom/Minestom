package net.minestom.server.codec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class CodecImpl {

    interface PrimitiveEncoder<T> {
        <D> @NotNull D encode(@NotNull Transcoder<D> coder, @NotNull T value);
    }

    @SuppressWarnings("unchecked")
    record PrimitiveImpl<T>(@NotNull PrimitiveEncoder<T> encoder, @NotNull Decoder<T> decoder) implements Codec<T> {
        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return decoder.decode(coder, value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            if (value == null) return new Result.Error<>("null");
            return (Result<D>) encoder.encode(coder, value);
        }
    }

    record OptionalImpl<T>(@NotNull Codec<T> inner, @Nullable T defaultValue) implements Codec<T> {
        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new Result.Ok<>(inner.decode(coder, value).orElse(defaultValue));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            if (value == null) return new Result.Ok<>(coder.createNull());
            return inner.encode(coder, value);
        }
    }

    record TransformImpl<T, S>(@NotNull Codec<T> inner, @NotNull Function<T, S> to,
                               @NotNull Function<S, T> from) implements Codec<S> {
        @Override
        public @NotNull <D> Result<S> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new Result.Ok<>(to.apply(inner.decode(coder, value).orElse(null)));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable S value) {
            return inner.encode(coder, from.apply(value));
        }
    }

    // TODO: do we want to do a mutable list builder & coder.getItem?
    record ListImpl<T>(@NotNull Codec<T> inner, int maxSize) implements Codec<List<T>> {
        @Override
        public @NotNull <D> Result<List<T>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<List<D>> result = coder.getList(value);
            if (!(result instanceof Result.Ok(List<D> list)))
                return result.cast();
            if (list.size() > maxSize)
                return new Result.Error<>("List size exceeds maximum allowed size: " + maxSize);
            final List<T> decodedList = new ArrayList<>();
            for (D item : list) {
                Result<T> decodedItem = inner.decode(coder, item);
                if (!(decodedItem instanceof Result.Ok(T valueItem)))
                    return decodedItem.cast();
                decodedList.add(valueItem);
            }
            return new Result.Ok<>(decodedList);
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder, @Nullable List<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (value.size() > maxSize)
                throw new IllegalArgumentException("List size exceeds maximum allowed size: " + maxSize);
            List<D> encodedList = new ArrayList<>(value.size());
            for (T item : value) {
                final Result<D> itemResult = inner.encode(coder, item);
                if (!(itemResult instanceof Result.Ok(D encodedItem)))
                    return itemResult.cast();
                encodedList.add(encodedItem);
            }
            return new Result.Ok<>(coder.createList(encodedList));
        }
    }

}
