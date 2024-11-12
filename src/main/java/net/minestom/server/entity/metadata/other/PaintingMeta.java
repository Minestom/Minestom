package net.minestom.server.entity.metadata.other;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Comparator;

public class PaintingMeta extends EntityMeta implements ObjectDataProvider {
    private Orientation orientation = null;

    public PaintingMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull DynamicRegistry.Key<Variant> getVariant() {
        return metadata.get(MetadataDef.Painting.VARIANT);
    }

    public void setVariant(@NotNull DynamicRegistry.Key<Variant> value) {
        metadata.set(MetadataDef.Painting.VARIANT, value);
    }

    @NotNull
    public Orientation getOrientation() {
        return this.orientation;
    }

    /**
     * Sets orientation of the painting.
     * This is possible only before spawn packet is sent.
     *
     * @param orientation the orientation of the painting.
     */
    public void setOrientation(@NotNull Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public int getObjectData() {
        Check.stateCondition(this.orientation == null, "Painting orientation must be set before spawn");
        return this.orientation.id();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }

    public enum Orientation {
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5);

        private final int id;

        Orientation(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }

    public sealed interface Variant extends ProtocolObject, PaintingVariants permits VariantImpl {
        @NotNull NetworkBuffer.Type<DynamicRegistry.Key<Variant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::paintingVariant);
        @NotNull BinaryTagSerializer<DynamicRegistry.Key<Variant>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::paintingVariant);

        static @NotNull Variant create(
                @NotNull NamespaceID assetId,
                int width, int height
        ) {
            return new VariantImpl(assetId, width, height, null);
        }

        static @NotNull Builder builder() {
            return new Builder();
        }

        /**
         * <p>Creates a new registry for painting variants, loading the vanilla painting variants.</p>
         *
         * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
         */
        @ApiStatus.Internal
        static @NotNull DynamicRegistry<Variant> createDefaultRegistry() {
            return DynamicRegistry.create(
                    "minecraft:painting_variant", VariantImpl.REGISTRY_NBT_TYPE, Registry.Resource.PAINTING_VARIANTS,
                    (namespace, props) -> new VariantImpl(Registry.paintingVariant(namespace, props)),
                    Comparator.naturalOrder()
            );
        }

        @NotNull NamespaceID assetId();

        int width();

        int height();

        @Override
        @Nullable Registry.PaintingVariantEntry registry();

        class Builder {
            private NamespaceID assetId;
            private int width;
            private int height;

            private Builder() {
            }

            @Contract(value = "_ -> this", pure = true)
            public @NotNull Builder assetId(@NotNull NamespaceID assetId) {
                this.assetId = assetId;
                return this;
            }

            @Contract(value = "_ -> this", pure = true)
            public @NotNull Builder width(int width) {
                this.width = width;
                return this;
            }

            @Contract(value = "_ -> this", pure = true)
            public @NotNull Builder height(int height) {
                this.height = height;
                return this;
            }

            public @NotNull Variant build() {
                return new VariantImpl(assetId, width, height, null);
            }
        }
    }

    record VariantImpl(
            @NotNull NamespaceID assetId,
            int width,
            int height,
            @Nullable Registry.PaintingVariantEntry registry
    ) implements Variant {
        private static final BinaryTagSerializer<Variant> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> {
                    throw new UnsupportedOperationException("PaintingVariant is read-only");
                },
                variant -> CompoundBinaryTag.builder()
                        .putString("asset_id", variant.assetId().asString())
                        .putInt("width", variant.width())
                        .putInt("height", variant.height())
                        .build()
        );

        @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
        VariantImpl {
            Check.argCondition(assetId == null, "missing asset id");
            Check.argCondition(width <= 0, "width must be positive");
            Check.argCondition(height <= 0, "height must be positive");
        }

        VariantImpl(@NotNull Registry.PaintingVariantEntry registry) {
            this(registry.assetId(), registry.width(), registry.height(), registry);
        }
    }

}
