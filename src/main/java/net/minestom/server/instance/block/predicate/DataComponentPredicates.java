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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public record DataComponentPredicates(DataComponentMap exact,
                                      Map<String, DataComponentPredicate> predicates) implements Predicate<DataComponent.Holder> {

    private static Codec<? extends DataComponentPredicate> getCodec(String type) {
        return switch (type) {
            case "damage" -> DataComponentPredicate.Damage.CODEC;
            case "enchantments" -> DataComponentPredicate.Enchantments.CODEC;
            case "stored_enchantments" -> DataComponentPredicate.StoredEnchantments.CODEC;
            case "potion_contents" -> DataComponentPredicate.Potions.CODEC;
            case "custom_data" -> DataComponentPredicate.CustomData.CODEC;
            case "container" -> DataComponentPredicate.Container.CODEC;
            case "bundle_contents" -> DataComponentPredicate.BundleContents.CODEC;
            case "firework_explosion" -> DataComponentPredicate.FireworkExplosion.CODEC;
            case "fireworks" -> DataComponentPredicate.Fireworks.CODEC;
            case "writable_book_content" -> DataComponentPredicate.WritableBook.CODEC;
            case "written_book_content" -> DataComponentPredicate.WrittenBook.CODEC;
            case "attribute_modifiers" -> DataComponentPredicate.AttributeModifiers.CODEC;
            case "trim" -> DataComponentPredicate.ArmorTrim.CODEC;
            case "jukebox_playable" -> DataComponentPredicate.JukeboxPlayable.CODEC;

            default -> throw new IllegalArgumentException("Unexpected component predicate type: " + type);
        };
    }

    /**
     * A list of data component predicate types as they appear in the MC source, used to populate their registry IDs for sending packets.
     */
    private static final List<String> COMPONENT_PREDICATE_REGISTRY_ORDER = List.of("damage", "enchantments", "stored_enchantments", "potion_contents", "custom_data", "container", "bundle_contents", "firework_explosion", "fireworks", "writable_book_content", "written_book_content", "attribute_modifiers", "trim", "jukebox_playable");

    private static int getRegistryId(String type) {
        return COMPONENT_PREDICATE_REGISTRY_ORDER.indexOf(type);
    }

    private static String getComponentPredicateType(int registryId) {
        return COMPONENT_PREDICATE_REGISTRY_ORDER.get(registryId);
    }

    private static final Codec<Map<String, DataComponentPredicate>> predicateCodec = Codec.NBT_COMPOUND.transform(
            nbt -> {
                if (nbt == null) return null;
                Map<String, DataComponentPredicate> map = new HashMap<>();
                final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
                for (var entry : nbt) {
                    Codec<? extends DataComponentPredicate> codec = getCodec(entry.getKey());
                    DataComponentPredicate value = codec.decode(coder, entry.getValue()).orElseThrow();
                    map.put(entry.getKey(), value);
                }
                return map;
            },
            map -> {
                if (map == null) return CompoundBinaryTag.empty();
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
                for (var entry : map.entrySet()) {
                    //noinspection unchecked
                    Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) getCodec(entry.getKey());
                    BinaryTag value = codec.encode(coder, entry.getValue()).orElseThrow();
                    builder.put(entry.getKey(), value);
                }
                return builder.build();
            });

    /**
     * A network type for a tuple of a {@link DataComponent}'s ID and its value.
     * A list of these is used to serialize {@link DataComponentPredicates#exact}.
     */
    private static final NetworkBuffer.Type<DataComponent.Value> componentNetworkType = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, DataComponent.Value value) {
            //noinspection unchecked
            DataComponent<Object> component = (DataComponent<Object>) value.component();
            NetworkBuffer.VAR_INT.write(buffer, component.id());
            component.write(buffer, value.value());
        }

        @Override
        public DataComponent.Value read(@NotNull NetworkBuffer buffer) {
            int componentId = NetworkBuffer.VAR_INT.read(buffer);
            DataComponent<?> component = DataComponent.fromId(componentId);
            Object value = component.read(buffer);
            return new DataComponent.Value(component, value);
        }
    };

    private static final NetworkBuffer.Type<DataComponentMap> componentsNetworkType = componentNetworkType.list().transform(
            list -> {
                DataComponentMap.Builder builder = DataComponentMap.builder();
                for (DataComponent.Value value : list) {
                    //noinspection unchecked
                    builder.set((DataComponent<Object>) value.component(), value.value());
                }
                return builder.build();
            },
            (DataComponentMap map) -> map == null ? List.of() : map.entrySet().stream().toList()
    );

    private static final NetworkBuffer.Type<Map<String, DataComponentPredicate>> predicateNetworkType = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Map<String, DataComponentPredicate> value) {
            if (value == null) {
                NetworkBuffer.VAR_INT.write(buffer, 0);
                return;
            }
            NetworkBuffer.VAR_INT.write(buffer, value.size());
            final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
            for (var entry : value.entrySet()) {
                int id = getRegistryId(entry.getKey());
                if (id == -1) continue;
                NetworkBuffer.VAR_INT.write(buffer, id);
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) getCodec(entry.getKey());
                BinaryTag tag = codec.encode(coder, entry.getValue()).orElseThrow();
                NetworkBuffer.NBT.write(buffer, tag);
            }
        }

        @Override
        public Map<String, DataComponentPredicate> read(@NotNull NetworkBuffer buffer) {
            Map<String, DataComponentPredicate> map = new HashMap<>();
            int size = NetworkBuffer.VAR_INT.read(buffer);
            final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
            for (int i = 0; i < size; i++) {
                int id = NetworkBuffer.VAR_INT.read(buffer);
                String type = getComponentPredicateType(id);
                //noinspection unchecked
                Codec<DataComponentPredicate> codec = (Codec<DataComponentPredicate>) getCodec(type);
                BinaryTag nbt = NetworkBuffer.NBT_COMPOUND.read(buffer);
                Result<DataComponentPredicate> result = codec.decode(coder, nbt);
                map.put(type, result.orElseThrow());
            }
            return map;
        }
    };

    public static final Codec<DataComponentPredicates> CODEC = StructCodec.struct(
            "components", DataComponent.PATCH_CODEC, DataComponentPredicates::exact,
            "predicates", predicateCodec, DataComponentPredicates::predicates,
            DataComponentPredicates::new
    );

    public static final NetworkBuffer.Type<DataComponentPredicates> NETWORK_TYPE = new NetworkBuffer.Type<>() {

        private static final NetworkBuffer.Type<DataComponentPredicates> delegate = NetworkBufferTemplate.template(
                componentsNetworkType, DataComponentPredicates::exact,
                predicateNetworkType, DataComponentPredicates::predicates,
                DataComponentPredicates::new
        );

        @Override
        public void write(@NotNull NetworkBuffer buffer, DataComponentPredicates value) {
            delegate.write(buffer, Objects.requireNonNullElseGet(value, () -> new DataComponentPredicates(null, null)));
        }

        @Override
        public DataComponentPredicates read(@NotNull NetworkBuffer buffer) {
            return delegate.read(buffer);
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
