package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class SpellcasterIllagerMeta extends AbstractIllagerMeta {
    protected SpellcasterIllagerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Spell getSpell() {
        return Spell.VALUES[metadata.get(MetadataDef.SpellcasterIllager.SPELL)];
    }

    public void setSpell(Spell spell) {
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
