package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class SpellcasterIllagerMeta extends AbstractIllagerMeta {
    public static final byte OFFSET = AbstractIllagerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    protected SpellcasterIllagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Spell getSpell() {
        return Spell.VALUES[super.metadata.getIndex(OFFSET, (byte) 0)];
    }

    public void setSpell(@NotNull Spell spell) {
        super.metadata.setIndex(OFFSET, Metadata.Byte((byte) spell.ordinal()));
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
