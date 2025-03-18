package net.minestom.server.codec;

import net.kyori.adventure.text.Component;
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

    record UnionImpl<T, R>(@NotNull String keyField, @NotNull Codec<T> keyCodec,
                           @NotNull Function<T, Codec<R>> serializers,
                           @NotNull Function<R, T> keyFunc) implements Codec<R> {
        @Override
        public @NotNull <D> Result<R> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<D> discriminantResult = coder.getValue(value, keyField);
            if (!(discriminantResult instanceof Result.Ok(D discriminant)))
                return discriminantResult.cast();
            if (discriminant == null) return new Result.Error<>("null");

            final Result<T> keyResult = keyCodec.decode(coder, discriminant);
            if (!(keyResult instanceof Result.Ok(T key)))
                return keyResult.cast();

            return serializers.apply(key).decode(coder, value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable R value) {
            if (value == null) return new Result.Error<>("null");

            final T key = keyFunc.apply(value);
            var serializer = serializers.apply(key);
            if (serializer == null) return new Result.Error<>("no union value: " + key);

            final Result<D> keyResult = keyCodec.encode(coder, key);
            if (!(keyResult instanceof Result.Ok(D keyValue)))
                return keyResult.cast();
            if (keyValue == null) return new Result.Error<>("null");

            final Result<D> serializedResult = serializer.encode(coder, value);
            if (!(serializedResult instanceof Result.Ok(D serializedValue)))
                return serializedResult.cast();
            if (serializedValue == null) return new Result.Error<>("null");

            return coder.putValue(serializedValue, keyField, keyValue);
        }
    }

    static final class RecursiveImpl<T> implements Codec<T> {
        private final Codec<T> delegate;

        public RecursiveImpl(@NotNull Function<Codec<T>, Codec<T>> self) {
            this.delegate = self.apply(this);
        }

        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return delegate.decode(coder, value);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            return delegate.encode(coder, value);
        }
    }

    record OrElseImpl<T>(@NotNull Codec<T> primary, @NotNull Codec<T> secondary) implements Codec<T> {
        @Override
        public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<T> primaryResult = primary.decode(coder, value);
            if (primaryResult instanceof Result.Ok<T> primaryOk)
                return primaryOk;

            // Primary did not work, try secondary
            final Result<T> secondaryResult = secondary.decode(coder, value);
            if (secondaryResult instanceof Result.Ok<T> secondaryOk)
                return secondaryOk;

            // Secondary did not work either, return error from primary.
            return primaryResult;
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
            return primary.encode(coder, value);
        }
    }

    record ComponentImpl() implements Codec<Component> {
        @Override
        public @NotNull <D> Result<Component> decode(@NotNull Transcoder<D> initCoder, @NotNull D value) {
            return null;
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Component value) {
            return null;
        }
    }

}
