package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record DataComponentPredicates(@Nullable DataComponentMap exact,
                                      @Nullable Map<ComponentPredicateType, DataComponentPredicate> predicates) implements Predicate<DataComponent.Holder> {

    public static DataComponentPredicates EMPTY = new DataComponentPredicates(null, null);

    public DataComponentPredicates {
        if (predicates != null) {
            predicates = Map.copyOf(predicates);
        }
    }

    public static final Codec<Range.Int> INT_RANGE_CODEC = StructCodec.struct(
            "min", Codec.INT, Range.Int::min,
            "max", Codec.INT, Range.Int::max,
            Range.Int::new
    ).orElse(Codec.INT.transform(Range.Int::new, Range.Int::min));

    public static final Codec<Range.Double> DOUBLE_RANGE_CODEC = StructCodec.struct(
            "min", Codec.DOUBLE, Range.Double::min,
            "max", Codec.DOUBLE, Range.Double::max,
            Range.Double::new
    ).orElse(Codec.DOUBLE.transform(Range.Double::new, Range.Double::min));

    public enum ComponentPredicateType {

        DAMAGE("damage", DataComponentPredicate.Damage.CODEC),
        ENCHANTMENTS("enchantments", DataComponentPredicate.Enchantments.CODEC),
        STORED_ENCHANTMENTS("stored_enchantments", DataComponentPredicate.StoredEnchantments.CODEC),
        POTION_CONTENTS("potion_contents", DataComponentPredicate.Potions.CODEC),
        CUSTOM_DATA("custom_data", DataComponentPredicate.CustomData.CODEC),
        CONTAINER("container", DataComponentPredicate.Container.CODEC),
        BUNDLE_CONTENTS("bundle_contents", DataComponentPredicate.BundleContents.CODEC),
        FIREWORK_EXPLOSION("firework_explosion", DataComponentPredicate.FireworkExplosion.CODEC),
        FIREWORKS("fireworks", DataComponentPredicate.Fireworks.CODEC),
        WRITABLE_BOOK_CONTENT("writable_book_content", DataComponentPredicate.WritableBook.CODEC),
        WRITTEN_BOOK_CONTENT("written_book_content", DataComponentPredicate.WrittenBook.CODEC),
        ATTRIBUTE_MODIFIERS("attribute_modifiers", DataComponentPredicate.AttributeModifiers.CODEC),
        TRIM("trim", DataComponentPredicate.ArmorTrim.CODEC),
        JUKEBOX_PLAYABLE("jukebox_playable", DataComponentPredicate.JukeboxPlayable.CODEC);

        private static final Map<String, ComponentPredicateType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ComponentPredicateType::getName, Function.identity()));
        public static final NetworkBuffer.Type<ComponentPredicateType> NETWORK_TYPE = NetworkBuffer.Enum(ComponentPredicateType.class);
        public static final Codec<ComponentPredicateType> CODEC = Codec.STRING.transform(BY_NAME::get, ComponentPredicateType::name);

        private final @NotNull String name;
        private final @NotNull Codec<? extends DataComponentPredicate> codec;

        ComponentPredicateType(@NotNull String name, @NotNull Codec<? extends DataComponentPredicate> codec) {
            this.name = name;
            this.codec = codec;
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull Codec<? extends DataComponentPredicate> getCodec() {
            return codec;
        }

        public static @Nullable ComponentPredicateType getByName(String name) {
            return BY_NAME.get(name);
        }

        public static @NotNull ComponentPredicateType getById(int id) {
            if (id < 0 || id >= values().length) {
                throw new IllegalArgumentException("Invalid ComponentPredicateType ID: " + id);
            }
            return values()[id];
        }
    }

    private static final Codec<Map<ComponentPredicateType, DataComponentPredicate>> predicateCodec = Codec.NBT_COMPOUND.transform(
            nbt -> {
                if (nbt == null) return null;
                Map<ComponentPredicateType, DataComponentPredicate> map = new HashMap<>();
                final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
                for (var entry : nbt) {
                    ComponentPredicateType type = ComponentPredicateType.getByName(entry.getKey());
                    if (type == null) {
                        throw new IllegalArgumentException("Invalid data component predicate type: " + entry.getKey());
                    }
                    Codec<? extends DataComponentPredicate> codec = type.getCodec();
                    DataComponentPredicate value = codec.decode(coder, entry.getValue()).orElseThrow();
                    map.put(type, value);
                }
                return map;
            },
            map -> {
                if (map == null) return CompoundBinaryTag.empty();
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
                for (var entry : map.entrySet()) {
                    //noinspection unchecked
                    Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) entry.getKey().getCodec();
                    BinaryTag value = codec.encode(coder, entry.getValue()).orElseThrow();
                    builder.put(entry.getKey().getName(), value);
                }
                return builder.build();
            });

    private static final NetworkBuffer.Type<Map<ComponentPredicateType, DataComponentPredicate>> predicateNetworkType = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Map<ComponentPredicateType, DataComponentPredicate> value) {
            if (value == null) {
                NetworkBuffer.VAR_INT.write(buffer, 0);
                return;
            }
            NetworkBuffer.VAR_INT.write(buffer, value.size());
            final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
            for (var entry : value.entrySet()) {
                ComponentPredicateType.NETWORK_TYPE.write(buffer, entry.getKey());
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) entry.getKey().getCodec();
                BinaryTag tag = codec.encode(coder, entry.getValue()).orElseThrow();
                NetworkBuffer.NBT.write(buffer, tag);
            }
        }

        @Override
        public Map<ComponentPredicateType, DataComponentPredicate> read(@NotNull NetworkBuffer buffer) {
            Map<ComponentPredicateType, DataComponentPredicate> map = new HashMap<>();
            int size = NetworkBuffer.VAR_INT.read(buffer);
            final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
            for (int i = 0; i < size; i++) {
                int id = NetworkBuffer.VAR_INT.read(buffer);
                ComponentPredicateType type = ComponentPredicateType.getById(id);
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) type.getCodec();
                BinaryTag nbt = NetworkBuffer.NBT_COMPOUND.read(buffer);
                Result<DataComponentPredicate> result = codec.decode(coder, nbt);
                map.put(type, result.orElseThrow());
            }
            return map;
        }
    };

    public static final Codec<DataComponentPredicates> CODEC = StructCodec.struct(
            "components", DataComponent.PATCH_CODEC.optional(), DataComponentPredicates::exact,
            "predicates", predicateCodec.optional(), DataComponentPredicates::predicates,
            DataComponentPredicates::new
    );

    public static final NetworkBuffer.Type<DataComponentPredicates> NETWORK_TYPE = new NetworkBuffer.Type<>() {

        private static final NetworkBuffer.Type<DataComponentMap> nullableComponentMapType = DataComponent.MAP_NETWORK_TYPE.transform(
                Function.identity(),
                map -> Objects.requireNonNullElse(map, DataComponentMap.EMPTY) // When encoding a DataComponentMap, if it's null, encode it as empty
        );

        private static final NetworkBuffer.Type<DataComponentPredicates> delegate = NetworkBufferTemplate.template(
                nullableComponentMapType, DataComponentPredicates::exact,
                predicateNetworkType, DataComponentPredicates::predicates,
                DataComponentPredicates::new
        );

        @Override
        public void write(@NotNull NetworkBuffer buffer, DataComponentPredicates value) {
            delegate.write(buffer, Objects.requireNonNullElseGet(value, () -> new DataComponentPredicates(null, null)));
        }

        @Override
        public DataComponentPredicates read(@NotNull NetworkBuffer buffer) {
            DataComponentPredicates value = delegate.read(buffer);
            DataComponentMap exact = value.exact();
            // Read empty lists and compounds as null
            if (exact != null && exact.isEmpty()) {
                exact = null;
            }
            Map<ComponentPredicateType, DataComponentPredicate> predicates = value.predicates();
            if (predicates != null && predicates.isEmpty()) {
                predicates = null;
            }
            return new DataComponentPredicates(exact, predicates);
        }
    };

    @Override
    public boolean test(DataComponent.Holder holder) {
        if (exact != null && !exact.entrySet().stream().allMatch(entry -> Objects.equals(holder.get(entry.component()), entry.value()))) {
            return false;
        }
        return predicates == null || predicates.entrySet().stream().allMatch(entry -> entry.getValue().test(holder));
    }
}
