package net.minestom.server.world.generation;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;

/**
 * A height expressed relative to one edge of the world rather than as a plain coordinate.
 * <p>
 * The value of every variant must lie between {@link DimensionType#MIN_Y} and {@link DimensionType#MAX_Y},
 * both inclusive. Constructing a variant outside that range throws {@link IllegalArgumentException}, and
 * decoding one fails.
 */
public sealed interface VerticalAnchor {
    Codec<VerticalAnchor> CODEC = Codec.Either(
            Codec.Either(Absolute.CODEC, AboveBottom.CODEC),
            Codec.Either(BelowTop.CODEC, RelativeToSeaLevel.CODEC)
    ).transform(
            outer -> outer.unify(Either::identity, Either::identity),
            anchor -> switch (anchor) {
                case Absolute absolute -> Either.left(Either.left(absolute));
                case AboveBottom aboveBottom -> Either.left(Either.right(aboveBottom));
                case BelowTop belowTop -> Either.right(Either.left(belowTop));
                case RelativeToSeaLevel relative -> Either.right(Either.right(relative));
            });

    private static void checkY(int y) {
        Check.argCondition(y < DimensionType.MIN_Y || y > DimensionType.MAX_Y,
                "Anchor value must be between " + DimensionType.MIN_Y + " and " + DimensionType.MAX_Y);
    }

    /**
     * A plain world coordinate.
     *
     * @param y the coordinate, between {@link DimensionType#MIN_Y} and {@link DimensionType#MAX_Y}
     */
    record Absolute(int y) implements VerticalAnchor {
        static final Codec<Absolute> CODEC = StructCodec.struct(
                "absolute", Codec.INT, Absolute::y,
                Absolute::new);

        public Absolute {
            checkY(y);
        }
    }

    /**
     * A height counted up from the lowest buildable block.
     *
     * @param offset the distance above the bottom, between {@link DimensionType#MIN_Y} and {@link DimensionType#MAX_Y}
     */
    record AboveBottom(int offset) implements VerticalAnchor {
        static final Codec<AboveBottom> CODEC = StructCodec.struct(
                "above_bottom", Codec.INT, AboveBottom::offset,
                AboveBottom::new);

        public AboveBottom {
            checkY(offset);
        }
    }

    /**
     * A height counted down from the highest buildable block.
     *
     * @param offset the distance below the top, between {@link DimensionType#MIN_Y} and {@link DimensionType#MAX_Y}
     */
    record BelowTop(int offset) implements VerticalAnchor {
        static final Codec<BelowTop> CODEC = StructCodec.struct(
                "below_top", Codec.INT, BelowTop::offset,
                BelowTop::new);

        public BelowTop {
            checkY(offset);
        }
    }

    /**
     * A height counted from the sea level of the dimension.
     *
     * @param offset the distance above sea level, between {@link DimensionType#MIN_Y} and {@link DimensionType#MAX_Y}
     */
    record RelativeToSeaLevel(int offset) implements VerticalAnchor {
        static final Codec<RelativeToSeaLevel> CODEC = StructCodec.struct(
                "relative_to_sea_level", Codec.INT, RelativeToSeaLevel::offset,
                RelativeToSeaLevel::new);

        public RelativeToSeaLevel {
            checkY(offset);
        }
    }
}
