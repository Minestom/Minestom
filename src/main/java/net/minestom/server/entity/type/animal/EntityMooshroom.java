package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.animal.MooshroomMeta} instead.
 */
@Deprecated
public class EntityMooshroom extends AgeableCreature implements Animal {

    public EntityMooshroom(Position spawnPosition) {
        super(EntityType.MOOSHROOM, spawnPosition);
        setBoundingBox(0.9f, 1.4f, 0.9f);
    }

    public MooshroomType getMooshroomType() {
        final String identifier = metadata.getIndex((byte) 16, "red");
        return MooshroomType.fromIdentifier(identifier);
    }

    public void setMooshroomType(@NotNull MooshroomType mooshroomType) {
        this.metadata.setIndex((byte) 16, Metadata.String(mooshroomType.getIdentifier()));
    }

    public enum MooshroomType {
        RED("red"),
        BROWN("brown");

        private final String identifier;

        MooshroomType(String identifier) {
            this.identifier = identifier;
        }

        @NotNull
        private String getIdentifier() {
            return identifier;
        }

        public static MooshroomType fromIdentifier(String identifier) {
            if (identifier.equals("red")) {
                return RED;
            } else if (identifier.equals("brown")) {
                return BROWN;
            }
            throw new IllegalArgumentException("Weird thing happened");
        }
    }
}
