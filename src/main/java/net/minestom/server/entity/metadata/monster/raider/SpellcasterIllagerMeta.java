package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class SpellcasterIllagerMeta extends AbstractIllagerMeta {

    protected SpellcasterIllagerMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public Spell getSpell() {
        return Spell.VALUES[getMetadata().getIndex((byte) 16, (byte) 0)];
    }

    public void setSpell(@NotNull Spell spell) {
        getMetadata().setIndex((byte) 16, Metadata.Byte((byte) spell.ordinal()));
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
