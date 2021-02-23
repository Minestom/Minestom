package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitySpellcasterIllager extends EntityRaider {

    protected EntitySpellcasterIllager(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    protected EntitySpellcasterIllager(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public Spell getSpell() {
        return Spell.VALUES[this.metadata.getIndex((byte) 16, (byte) 0)];
    }

    public void setSpell(Spell spell) {
        this.metadata.setIndex((byte) 16, Metadata.Byte((byte) spell.ordinal()));
    }

    public enum Spell {
        NONE,
        SUMMON_VEX,
        ATTACK,
        WOLOLO,
        DISAPPEAR,
        BLINDNESS;

        private final static Spell[] VALUES = values();
    }

}
