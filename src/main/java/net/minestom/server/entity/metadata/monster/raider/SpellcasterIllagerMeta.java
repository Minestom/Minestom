package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class SpellcasterIllagerMeta extends AbstractIllagerMeta {
    protected SpellcasterIllagerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Spell getSpell() {
        return Spell.VALUES[metadata.get(MetadataDef.SpellcasterIllager.SPELL)];
    }

    public void setSpell(@NotNull Spell spell) {
        metadata.set(MetadataDef.SpellcasterIllager.SPELL, (byte) spell.ordinal());
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
