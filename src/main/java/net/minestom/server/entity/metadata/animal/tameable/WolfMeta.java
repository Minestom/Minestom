package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WolfMeta extends TameableAnimalMeta {
    public WolfMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isBegging() {
        return metadata.get(MetadataDef.Wolf.IS_BEGGING);
    }

    public void setBegging(boolean value) {
        metadata.set(MetadataDef.Wolf.IS_BEGGING, value);
    }

    public int getCollarColor() {
        return metadata.get(MetadataDef.Wolf.COLLAR_COLOR);
    }

    public void setCollarColor(int value) {
        metadata.set(MetadataDef.Wolf.COLLAR_COLOR, value);
    }

    public int getAngerTime() {
        return metadata.get(MetadataDef.Wolf.ANGER_TIME);
    }

    public void setAngerTime(int value) {
        metadata.set(MetadataDef.Wolf.ANGER_TIME, value);
    }

    public @NotNull DynamicRegistry.Key<Variant> getVariant() {
        return metadata.get(MetadataDef.Wolf.VARIANT);
    }

    public void setVariant(@NotNull DynamicRegistry.Key<Variant> value) {
        metadata.set(MetadataDef.Wolf.VARIANT, value);
    }

    public sealed interface Variant extends ProtocolObject, WolfVariants permits VariantImpl {
        @NotNull NetworkBuffer.Type<DynamicRegistry.Key<Variant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::wolfVariant);
        @NotNull BinaryTagSerializer<DynamicRegistry.Key<Variant>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::wolfVariant);

        static @NotNull Variant create(
                @NotNull NamespaceID wildTexture,
                @NotNull NamespaceID tameTexture,
                @NotNull NamespaceID angryTexture,
                @NotNull String biome
        ) {
            return new VariantImpl(wildTexture, tameTexture, angryTexture, List.of(biome), null);
        }

        static @NotNull Builder builder() {
            return new Builder();
        }

        /**
         * <p>Creates a new registry for wolf variants, loading the vanilla wolf variants.</p>
         *
         * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
         */
        @ApiStatus.Internal
        static @NotNull DynamicRegistry<Variant> createDefaultRegistry() {
            return DynamicRegistry.create(
                    "minecraft:wolf_variant", VariantImpl.REGISTRY_NBT_TYPE, Registry.Resource.WOLF_VARIANTS,
                    (namespace, props) -> new WolfMeta.VariantImpl(Registry.wolfVariant(namespace, props))
            );
        }

        @NotNull NamespaceID wildTexture();

        @NotNull NamespaceID tameTexture();

        @NotNull NamespaceID angryTexture();

        @NotNull List<String> biomes();

        @Override
        @Nullable Registry.WolfVariantEntry registry();

        final class Builder {
            private NamespaceID wildTexture;
            private NamespaceID tameTexture;
            private NamespaceID angryTexture;
            private List<String> biomes;

            private Builder() {
            }

            public @NotNull Builder wildTexture(@NotNull NamespaceID wildTexture) {
                this.wildTexture = wildTexture;
                return this;
            }

            public @NotNull Builder tameTexture(@NotNull NamespaceID tameTexture) {
                this.tameTexture = tameTexture;
                return this;
            }

            public @NotNull Builder angryTexture(@NotNull NamespaceID angryTexture) {
                this.angryTexture = angryTexture;
                return this;
            }

            public @NotNull Builder biome(@NotNull String biome) {
                this.biomes = List.of(biome);
                return this;
            }

            public @NotNull Builder biomes(@NotNull List<String> biomes) {
                this.biomes = biomes;
                return this;
            }

            public @NotNull Variant build() {
                return new VariantImpl(wildTexture, tameTexture, angryTexture, biomes, null);
            }
        }
    }

    record VariantImpl(
            @NotNull NamespaceID wildTexture,
            @NotNull NamespaceID tameTexture,
            @NotNull NamespaceID angryTexture,
            @NotNull List<String> biomes,
            @Nullable Registry.WolfVariantEntry registry
    ) implements Variant {

        private static final BinaryTagSerializer<List<String>> BIOMES_NBT_TYPE = BinaryTagSerializer.STRING.list();
        static final BinaryTagSerializer<Variant> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> {
                    throw new UnsupportedOperationException("WolfVariant is read-only");
                },
                wolfVariant -> {
                    BinaryTag biomes;
                    if (wolfVariant.biomes().size() == 1) {
                        biomes = StringBinaryTag.stringBinaryTag(wolfVariant.biomes().getFirst());
                    } else {
                        biomes = BIOMES_NBT_TYPE.write(wolfVariant.biomes());
                    }
                    return CompoundBinaryTag.builder()
                            .putString("wild_texture", wolfVariant.wildTexture().asString())
                            .putString("tame_texture", wolfVariant.tameTexture().asString())
                            .putString("angry_texture", wolfVariant.angryTexture().asString())
                            .put("biomes", biomes)
                            .build();
                }
        );

        VariantImpl {
            // The builder can violate the nullability constraints
            Check.notNull(wildTexture, "missing wild texture");
            Check.notNull(tameTexture, "missing tame texture");
            Check.notNull(angryTexture, "missing angry texture");
            Check.notNull(biomes, "missing biomes");
        }

        VariantImpl(@NotNull Registry.WolfVariantEntry registry) {
            this(registry.wildTexture(), registry.tameTexture(),
                    registry.angryTexture(), registry.biomes(), registry);
        }
    }

}
