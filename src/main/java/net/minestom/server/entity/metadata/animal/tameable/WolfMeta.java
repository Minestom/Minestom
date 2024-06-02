package net.minestom.server.entity.metadata.animal.tameable;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.DynamicRegistryImpl;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WolfMeta extends TameableAnimalMeta {
    public static final byte OFFSET = TameableAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public WolfMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    //todo variant

    public boolean isBegging() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setBegging(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

    public int getCollarColor() {
        return super.metadata.getIndex(OFFSET + 1, 14);
    }

    public void setCollarColor(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public int getAngerTime() {
        return super.metadata.getIndex(OFFSET + 2, 0);
    }

    public void setAngerTime(int value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value));
    }

    public sealed interface Variant extends ProtocolObject, WolfVariants permits VariantImpl {
        @NotNull BinaryTagSerializer<Variant> NBT_TYPE = VariantImpl.NBT_TYPE;

        static @NotNull Variant create(
                @NotNull NamespaceID namespace,
                @NotNull NamespaceID wildTexture,
                @NotNull NamespaceID tameTexture,
                @NotNull NamespaceID angryTexture,
                @NotNull String biome
        ) {
            return new VariantImpl(namespace, wildTexture, tameTexture, angryTexture, List.of(biome), null);
        }

        static @NotNull Builder builder(@NotNull String namespace) {
            return builder(NamespaceID.from(namespace));
        }

        static @NotNull Builder builder(@NotNull NamespaceID namespace) {
            return new Builder(namespace);
        }

        /**
         * <p>Creates a new registry for wolf variants, loading the vanilla wolf variants.</p>
         *
         * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
         */
        @ApiStatus.Internal
        static @NotNull DynamicRegistry<Variant> createDefaultRegistry() {
            return new DynamicRegistryImpl<>(
                    "minecraft:wolf_variant", NBT_TYPE, Registry.Resource.WOLF_VARIANTS,
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
            private final NamespaceID namespace;
            private NamespaceID wildTexture;
            private NamespaceID tameTexture;
            private NamespaceID angryTexture;
            private List<String> biomes;

            private Builder(@NotNull NamespaceID namespace) {
                this.namespace = namespace;
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
                return new VariantImpl(namespace, wildTexture, tameTexture, angryTexture, biomes, null);
            }
        }
    }

    record VariantImpl(
            @NotNull NamespaceID namespace,
            @NotNull NamespaceID wildTexture,
            @NotNull NamespaceID tameTexture,
            @NotNull NamespaceID angryTexture,
            @NotNull List<String> biomes,
            @Nullable Registry.WolfVariantEntry registry
    ) implements Variant {

        private static final BinaryTagSerializer<List<String>> BIOMES_NBT_TYPE = BinaryTagSerializer.STRING.list();
        static final BinaryTagSerializer<Variant> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
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
            Check.notNull(namespace, "Namespace cannot be null");
            Check.notNull(wildTexture, "missing wild texture: {0}", namespace);
            Check.notNull(tameTexture, "missing tame texture: {0}", namespace);
            Check.notNull(angryTexture, "missing angry texture: {0}", namespace);
            Check.notNull(biomes, "missing biomes: {0}", namespace);
        }

        VariantImpl(@NotNull Registry.WolfVariantEntry registry) {
            this(registry.namespace(), registry.wildTexture(), registry.tameTexture(),
                    registry.angryTexture(), registry.biomes(), registry);
        }
    }

}
