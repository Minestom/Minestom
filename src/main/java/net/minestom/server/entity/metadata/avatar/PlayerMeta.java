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
        return metadata.get(MetadataDef.Player.ADDITIONAL_HEARTS);
    }

    public void setAdditionalHearts(float value) {
        metadata.set(MetadataDef.Player.ADDITIONAL_HEARTS, value);
    }

    public int getScore() {
        return metadata.get(MetadataDef.Player.SCORE);
    }

    public void setScore(int value) {
        metadata.set(MetadataDef.Player.SCORE, value);
    }

    public @Nullable Integer getLeftShoulderEntityData() {
        return metadata.get(MetadataDef.Player.LEFT_SHOULDER_ENTITY_DATA);
    }

    public void setLeftShoulderEntityData(@Nullable Integer value) {
        metadata.set(MetadataDef.Player.LEFT_SHOULDER_ENTITY_DATA, value);
    }

    public @Nullable Integer getRightShoulderEntityData() {
        return metadata.get(MetadataDef.Player.RIGHT_SHOULDER_ENTITY_DATA);
    }

    public void setRightShoulderEntityData(@Nullable Integer value) {
        metadata.set(MetadataDef.Player.RIGHT_SHOULDER_ENTITY_DATA, value);
    }

}
