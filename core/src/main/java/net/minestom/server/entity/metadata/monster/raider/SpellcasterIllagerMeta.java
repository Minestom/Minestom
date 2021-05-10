package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class SpellcasterIllagerMeta extends AbstractIllagerMeta {

    protected SpellcasterIllagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Spell getSpell() {
        return Spell.VALUES[super.metadata.getIndex((byte) 16, (byte) 0)];
    }

    public void setSpell(@NotNull Spell spell) {
        super.metadata.setIndex((byte) 16, Metadata.Byte((byte) spell.ordinal()));
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
