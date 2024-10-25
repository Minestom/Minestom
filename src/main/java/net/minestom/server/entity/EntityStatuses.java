package net.minestom.server.entity;

/**
 * Entity status ids used in {@link net.minestom.server.network.packet.server.play.EntityStatusPacket}.
 */
@SuppressWarnings("ALL")
public sealed class EntityStatuses {
    public static final int SPAWNS_HONEY_BLOCK_PARTICLES = 53;

    public static final class Arrow extends EntityStatuses {
        public static final int SPAWN_TIPPED_ARROW_PARTICLE = 0;
    }

    public static sealed class LivingEntity extends EntityStatuses {
        public static final int PLAY_DEATH_SOUND = 3;
        public static final int PLAY_SHIELD_BLOCK_SOUND = 29;
        public static final int PLAY_SHIELD_BREAK_SOUND = 30;
        public static final int PLAY_TOTEM_UNDYING_ANIMATION_SOUND = 35;

        public static final int SWAP_HAND_ITEMS = 55;
        public static final int SPAWN_DEATH_SMOKE_PARTICLES = 60;
    }

    public static final class Player extends LivingEntity {
        public static final int MARK_ITEM_FINISHED = 9;
        public static final int ENABLE_DEBUG_SCREEN = 22;
        public static final int DISABLE_DEBUG_SCREEN = 23;
        public static final int PERMISSION_LEVEL_0 = 24;
        public static final int PERMISSION_LEVEL_1 = 25;
        public static final int PERMISSION_LEVEL_2 = 26;
        public static final int PERMISSION_LEVEL_3 = 27;
        public static final int PERMISSION_LEVEL_4 = 28;
        public static final int SPAWN_CLOUD_PARTICLES = 43;
    }

    public static sealed class Animal extends EntityStatuses {
        public static final int SPAWN_LOVE_MODE_PARTICLES = 18;
    }

    public static final class Ocelot extends Animal {
        public static final int SPAWN_SMOKE_PARTICLES = 40;
        public static final int SPAWN_HEART_PARTICLES = 41;
    }

    public static final class Rabbit extends Animal {
        public static final int JUMP_ANIMATION = 1;
    }

    public static final class Sheep extends Animal {
        public static final int EAT_GRASS = 10;
    }

    public static final class Sniffer extends Animal {
        public static final int PLAY_DIGGING_SOUND = 63;
    }
}
