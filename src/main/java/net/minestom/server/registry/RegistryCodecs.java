package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.utils.Either;
import org.intellij.lang.annotations.Subst;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class RegistryCodecs {

    record RegistryKeyImpl<T>(Registries.Selector<T> selector) implements Codec<RegistryKey<T>> {
        @Override
        public <D> Result<RegistryKey<T>> decode(Transcoder<D> coder, D value) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = selector.select(context.registries());
            final Result<String> referenceResult = coder.getString(value);
            if (!(referenceResult instanceof Result.Ok(@Subst("a")String reference)))
                return referenceResult.cast();
            final RegistryKey<T> key = registry.getKey(Key.key(reference));
            if (key == null) return new Result.Error<>("Unknown key " + reference + " for registry " + registry.key());
            return new Result.Ok<>(key);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable RegistryKey<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (!(coder instanceof RegistryTranscoder<D>))
                return new Result.Error<>("Missing registries in transcoder");
            return new Result.Ok<>(coder.createString(value.key().asString()));
        }
    }

    record HolderCodec<T>(
            Registries.Selector<T> selector,
            Codec<T> registryCodec
    ) implements Codec<Holder<T>> {
        @Override
        public <D> Result<Holder<T>> decode(Transcoder<D> coder, D value) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = selector.select(context.registries());
            final Result<T> directResult = registryCodec.decode(coder, value);
            if (directResult instanceof Result.Ok(T direct))
                //noinspection unchecked
                return new Result.Ok<>((Holder<T>) direct);
            final Result<String> referenceResult = coder.getString(value);
            if (!(referenceResult instanceof Result.Ok(@Subst("a")String reference)))
                return referenceResult.cast();
            final RegistryKey<T> key = registry.getKey(Key.key(reference));
            if (key == null) return new Result.Error<>("Unknown key " + reference + " for registry " + registry.key());
            return new Result.Ok<>(key);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Holder<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (!(coder instanceof RegistryTranscoder<D>))
                return new Result.Error<>("Missing registries in transcoder");
            return switch (value.unwrap()) {
                case Either.Left(RegistryKey<T> key) -> new Result.Ok<>(coder.createString(key.key().asString()));
                case Either.Right(T direct) -> registryCodec.encode(coder, direct);
            };
        }
    }

    record TagKeyImpl<T>(Registries.Selector<T> selector, boolean hash) implements Codec<TagKey<T>> {
        @Override
        public <D> Result<TagKey<T>> decode(Transcoder<D> coder, D value) {
            if (!(coder instanceof RegistryTranscoder<D> context))
                return new Result.Error<>("Missing registries in transcoder");
            final var registry = selector.select(context.registries());
            final var result = coder.getString(value);
            if (!(result instanceof Result.Ok(@Subst("a")String reference)))
                return result.cast();
            if (hash) {
                if (reference.length() < 2 || reference.charAt(0) != '#')
                    return new Result.Error<>("Invalid tag hash: " + reference);
                reference = reference.substring(1);
            }
            final TagKey<T> tagKey = new net.minestom.server.registry.TagKeyImpl<>(Key.key(reference));
            if (registry.getTag(tagKey) == null)
                return new Result.Error<>("Unknown tag " + reference + " for registry " + registry.key());
            return new Result.Ok<>(tagKey);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable TagKey<T> value) {
            if (value == null) return new Result.Error<>("null");
            if (!(coder instanceof RegistryTranscoder<D>))
                return new Result.Error<>("Missing registries in transcoder");
            return new Result.Ok<>(coder.createString(hash ? value.hashedKey() : value.key().asString()));
        }
    }

    record RegistryTagImpl<T>(Registries.Selector<T> selector) implements Codec<RegistryTag<T>> {
        // Per vanilla, this codec supports registryless context, in which case it can only decode direct tags.

        @Override
        public <D> Result<RegistryTag<T>> decode(Transcoder<D> coder, D value) {
            final var context = coder instanceof RegistryTranscoder<D> transcoder ? transcoder : null;
            final var registry = context != null ? selector.select(context.registries()) : null;
            final Result<String> tagKeyResult = coder.getString(value);
            if (tagKeyResult instanceof Result.Ok(String tagKeyStr)) {
                if (registry != null && tagKeyStr.startsWith("#")) {
                    final var tagKey = TagKey.<T>ofHash(tagKeyStr);
                    // During initialization of the registry we allow creating tags that do not exist yet, otherwise we do not.
                    final var tag = context.init() ? registry.getOrCreateTag(tagKey) : registry.getTag(tagKey);
                    return tag != null ? new Result.Ok<>(tag)
                            : new Result.Error<>("Unknown tag " + tagKey + " for registry " + registry.key());
                }
                return new Result.Ok<>(RegistryTag.direct(RegistryKey.unsafeOf(tagKeyStr)));
            }
            final Result<List<D>> entriesResult = coder.getList(value);
            if (entriesResult instanceof Result.Ok(List<D> entries)) {
                final Set<RegistryKey<T>> keys = new HashSet<>(entries.size());
                for (D entry : entries) {
                    final Result<String> keyResult = coder.getString(entry);
                    if (!(keyResult instanceof Result.Ok(@Subst("a")String key)))
                        return keyResult.mapError(e -> "Invalid tag entry: " + e).cast();
                    final RegistryKey<T> registryKey = registry != null ? registry.getKey(Key.key(key)) : RegistryKey.unsafeOf(key);
                    if (registryKey == null)
                        return new Result.Error<>("Unknown key " + key + " for registry " + registry.key());
                    keys.add(registryKey);
                }
                return new Result.Ok<>(RegistryTag.direct(keys));
            }

            return new Result.Error<>("Invalid tag value: " + value);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable RegistryTag<T> value) {
            if (value == null) return new Result.Error<>("null");
            return switch (value) {
                case net.minestom.server.registry.RegistryTagImpl.Backed<T> backed ->
                        new Result.Ok<>(coder.createString(backed.key().hashedKey()));
                case net.minestom.server.registry.RegistryTagImpl.Empty() -> new Result.Ok<>(coder.emptyList());
                case net.minestom.server.registry.RegistryTagImpl.Direct(var entries) -> {
                    if (entries.isEmpty()) yield new Result.Ok<>(coder.emptyList());
                    if (entries.size() == 1)
                        yield new Result.Ok<>(coder.createString(entries.getFirst().key().asString()));
                    final Transcoder.ListBuilder<D> result = coder.createList(entries.size());
                    for (final RegistryKey<T> key : entries)
                        result.add(coder.createString(key.key().asString()));
                    yield new Result.Ok<>(result.build());
                }
            };
        }
    }

    record HolderSetImpl<T extends Holder<T>>(
            Codec<RegistryTag<T>> tagCodec,
            Codec<T> directCodec
    ) implements Codec<HolderSet<T>> {
        @Override
        public <D> Result<HolderSet<T>> decode(Transcoder<D> coder, D value) {
            // First try to decode as a tag
            final Result<RegistryTag<T>> tagResult = tagCodec.decode(coder, value);
            if (tagResult instanceof Result.Ok(RegistryTag<T> tag))
                return new Result.Ok<>(tag);

            // Otherwise try to decode as a direct holder set
            final Result<List<D>> entriesResult = coder.getList(value);
            if (!(entriesResult instanceof Result.Ok(List<D> entries)))
                return entriesResult.mapError(e -> "Invalid holder set value: " + e).cast();

            final List<T> directEntries = new ArrayList<>(entries.size());
            for (D entry : entries) {
                final Result<T> directResult = directCodec.decode(coder, entry);
                if (directResult instanceof Result.Ok(T direct)) {
                    directEntries.add(direct);
                } else {
                    return directResult.mapError(e -> "Invalid holder set entry: " + e).cast();
                }
            }
            // This raw type is kinda gross. Its safe because direct is checked only
            // to be instantiated with Holder.Direct types, but HolderSet itself supports non-direct types.
            //noinspection rawtypes,unchecked
            return new Result.Ok<>((HolderSet<T>) new HolderSet.Direct(directEntries));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable HolderSet<T> value) {
            if (value == null) return new Result.Error<>("null");
            return switch (value) {
                case RegistryTag<T> tag -> tagCodec.encode(coder, tag);
                // This raw type is kinda gross. Its safe because direct is checked only
                // to be instantiated with Holder.Direct types, but HolderSet itself supports non-direct types.
                //noinspection rawtypes
                case HolderSet.Direct d -> {
                    final Transcoder.ListBuilder<D> result = coder.createList(d.values().size());
                    for (final Object rawValue : d.values()) {
                        final var directResult = directCodec.encode(coder, (T) rawValue);
                        if (directResult instanceof Result.Ok(D direct)) {
                            result.add(direct);
                        } else {
                            yield directResult.mapError(e -> "Invalid holder set entry: " + e).cast();
                        }
                    }
                    yield new Result.Ok<>(result.build());
                }
            };
        }
    }
}
