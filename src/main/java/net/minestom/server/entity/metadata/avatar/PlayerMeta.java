package net.minestom.server.entity.metadata.avatar;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public final class PlayerMeta extends AvatarMeta {
    public PlayerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public float getAdditionalHearts() {
        return get(MetadataDef.Player.ADDITIONAL_HEARTS);
    }

    public void setAdditionalHearts(float value) {
        set(MetadataDef.Player.ADDITIONAL_HEARTS, value);
    }

    public int getScore() {
        return get(MetadataDef.Player.SCORE);
    }

    public void setScore(int value) {
        set(MetadataDef.Player.SCORE, value);
    }

    public @Nullable Integer getLeftShoulderEntityData() {
        return get(MetadataDef.Player.LEFT_SHOULDER_ENTITY_DATA);
    }

    public void setLeftShoulderEntityData(@Nullable Integer value) {
        set(MetadataDef.Player.LEFT_SHOULDER_ENTITY_DATA, value);
    }

    public @Nullable Integer getRightShoulderEntityData() {
        return get(MetadataDef.Player.RIGHT_SHOULDER_ENTITY_DATA);
    }

    public void setRightShoulderEntityData(@Nullable Integer value) {
        set(MetadataDef.Player.RIGHT_SHOULDER_ENTITY_DATA, value);
    }

}
