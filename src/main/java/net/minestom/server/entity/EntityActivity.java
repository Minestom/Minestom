package net.minestom.server.entity;

import net.minestom.server.codec.Codec;

public enum EntityActivity {
    CORE,
    IDLE,
    WORK,
    PLAY,
    REST,
    MEET,
    PANIC,
    RAID,
    PRE_RAID,
    HIDE,
    FIGHT,
    CELEBRATE,
    ADMIRE_ITEM,
    AVOID,
    RIDE,
    PLAY_DEAD,
    LONG_JUMP,
    RAM,
    TONGUE,
    SWIM,
    LAY_SPAWN,
    SNIFF,
    INVESTIGATE,
    ROAR,
    EMERGE,
    DIG;

    public static final Codec<EntityActivity> CODEC = Codec.Enum(EntityActivity.class);
}
