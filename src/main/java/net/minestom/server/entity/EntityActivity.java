package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum EntityActivity implements Keyed {
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

    private static final Map<Key, EntityActivity> BY_KEY = Arrays.stream(values())
            .collect(Collectors.toMap(Keyed::key, Function.identity()));

    public static final Codec<EntityActivity> CODEC = Codec.KEY.transform(BY_KEY::get, Keyed::key);

    private final Key key = Key.key(name().toLowerCase(Locale.ROOT));

    @Override
    public Key key() {
        return key;
    }
}
