package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class PaintingMeta extends EntityMeta implements ObjectDataProvider {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private Orientation orientation = null;

    public PaintingMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public @NotNull Variant getVariant() {
        return super.metadata.getIndex(OFFSET, Variant.KEBAB);
    }

    public void setVariant(@NotNull Variant value) {
        super.metadata.setIndex(OFFSET, Metadata.PaintingVariant(value));
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

    public enum Variant implements StaticProtocolObject {
        KEBAB(16, 16),
        AZTEC(16, 16),
        ALBAN(16, 16),
        AZTEC2(16, 16),
        BOMB(16, 16),
        PLANT(16, 16),
        WASTELAND(16, 16),
        POOL(32, 16),
        COURBET(32, 16),
        SEA(32, 16),
        SUNSET(32, 16),
        CREEBET(32, 16),
        WANDERER(16, 32),
        GRAHAM(16, 32),
        MATCH(32, 32),
        BUST(32, 32),
        STAGE(32, 32),
        VOID(32, 32),
        SKULL_AND_ROSES(32, 32),
        WITHER(32, 32),
        FIGHTERS(64, 32),
        POINTER(64, 64),
        PIGSCENE(64, 64),
        BURNING_SKULL(64, 64),
        SKELETON(64, 48),
        EARTH(32, 32),
        WIND(32, 32),
        WATER(32, 32),
        FIRE(32, 32),
        DONKEY_KONG(64, 48);

        private static final Variant[] VALUES = values();

        public static @Nullable Variant fromId(int id) {
            if (id < 0 || id >= VALUES.length) {
                return null;
            }
            return VALUES[id];
        }

        public static @Nullable Variant fromNamespaceId(@Nullable String namespaceId) {
            if (namespaceId == null) return null;
            return fromNamespaceId(NamespaceID.from(namespaceId));
        }

        public static @Nullable Variant fromNamespaceId(@Nullable NamespaceID namespaceId) {
            if (namespaceId == null) return null;
            for (Variant value : VALUES) {
                if (value.namespace().equals(namespaceId)) {
                    return value;
                }
            }
            return null;
        }

        private final NamespaceID namespace;
        private final int width;
        private final int height;

        Variant(int width, int height) {
            this.namespace = NamespaceID.from("minecraft", name().toLowerCase(Locale.ROOT));
            this.width = width;
            this.height = height;
        }

        @Override
        public int id() {
            return ordinal();
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        @Override
        public @NotNull NamespaceID namespace() {
            return namespace;
        }
    }

}
