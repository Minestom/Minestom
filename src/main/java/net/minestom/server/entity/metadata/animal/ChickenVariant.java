package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface ChickenVariant extends ChickenVariants permits ChickenVariantImpl {

    Codec<ChickenVariant> REGISTRY_CODEC = StructCodec.struct(
            "model", Model.CODEC.optional(Model.NORMAL), ChickenVariant::model,
            "asset_id", Codec.KEY, ChickenVariant::assetId,
            ChickenVariantImpl::new);

    // The type of a chicken variant is an EitherHolder, except that the Holder is only a reference and does not add 1 to the value
    // Effectively this type just allows decoding as a Key or an ID in the registry. Its implemented inline here
    // because this pattern is not currently used anywhere else to my(matt) knowledge.
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<ChickenVariant>> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        private final NetworkBuffer.Type<DynamicRegistry.Key<ChickenVariant>> idCodec = NetworkBuffer.RegistryKey(Registries::chickenVariant, false);

        @Override
        public void write(@NotNull NetworkBuffer buffer, DynamicRegistry.Key<ChickenVariant> value) {
            final var registries = buffer.registries();
            final int id = registries != null ? registries.chickenVariant().getId(value) : -1;
            if (id == -1) {
                buffer.write(NetworkBuffer.BOOLEAN, false);
                buffer.write(NetworkBuffer.KEY, value.key());
            } else {
                buffer.write(NetworkBuffer.BOOLEAN, true);
                buffer.write(NetworkBuffer.VAR_INT, id);
            }
        }

        @Override
        public DynamicRegistry.Key<ChickenVariant> read(@NotNull NetworkBuffer buffer) {
            if (buffer.read(NetworkBuffer.BOOLEAN))
                return buffer.read(idCodec);
            return DynamicRegistry.Key.of(buffer.read(NetworkBuffer.KEY));
        }
    };
    @NotNull Codec<DynamicRegistry.Key<ChickenVariant>> CODEC = Codec.RegistryKey(Registries::chickenVariant);

    static @NotNull ChickenVariant create(@NotNull Model model, @NotNull Key assetId) {
        return new ChickenVariantImpl(model, assetId);
    }

    /**
     * Creates a new instance of the "minecraft:chicken_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<ChickenVariant> createDefaultRegistry() {
        return DynamicRegistry.create("minecraft:chicken_variant", REGISTRY_CODEC, RegistryData.Resource.CHICKEN_VARIANTS);
    }

    @NotNull Model model();

    @NotNull Key assetId();

    enum Model {
        NORMAL,
        COLD;

        public static final Codec<Model> CODEC = Codec.Enum(Model.class);
    }
}
