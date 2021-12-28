package net.minestom.server.tags;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public final class EntityTypeGameTags {

    public static final @NotNull GameTag<@NotNull EntityType> SKELETONS = get("skeletons");
    public static final @NotNull GameTag<@NotNull EntityType> RAIDERS = get("raiders");
    public static final @NotNull GameTag<@NotNull EntityType> BEEHIVE_INHABITORS = get("beehive_inhabitors");
    public static final @NotNull GameTag<@NotNull EntityType> ARROWS = get("arrows");
    public static final @NotNull GameTag<@NotNull EntityType> IMPACT_PROJECTILES = get("impact_projectiles");
    public static final @NotNull GameTag<@NotNull EntityType> POWDER_SNOW_WALKABLE_MOBS = get("powder_snow_walkable_mobs");
    public static final @NotNull GameTag<@NotNull EntityType> AXOLOTL_ALWAYS_HOSTILES = get("axolotl_always_hostiles");
    public static final @NotNull GameTag<@NotNull EntityType> AXOLOTL_HUNT_TARGETS = get("axolotl_hunt_targets");
    public static final @NotNull GameTag<@NotNull EntityType> FREEZE_IMMUNE_ENTITY_TYPES = get("freeze_immune_entity_types");
    public static final @NotNull GameTag<@NotNull EntityType> FREEZE_HURTS_EXTRA_TYPES = get("freeze_hurts_extra_types");

    private static GameTag<EntityType> get(final String name) {
        return MinecraftServer.getTagManager().get(GameTagType.ENTITY_TYPES, "minecraft:" + name);
    }

    private EntityTypeGameTags() {
    }
}
