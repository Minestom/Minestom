package net.minestom.server.entity.metadata.avatar;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.player.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

public final class MannequinMeta extends AvatarMeta {

    public MannequinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ResolvableProfile getProfile() {
        return get(MetadataDef.Mannequin.PROFILE);
    }

    public void setProfile(ResolvableProfile value) {
        set(MetadataDef.Mannequin.PROFILE, value);
    }

    public boolean isImmovable() {
        return get(MetadataDef.Mannequin.IMMOVABLE);
    }

    public void setImmovable(boolean value) {
        set(MetadataDef.Mannequin.IMMOVABLE, value);
    }

    public @Nullable Component getDescription() {
        return get(MetadataDef.Mannequin.DESCRIPTION);
    }

    public void setDescription(@Nullable Component value) {
        set(MetadataDef.Mannequin.DESCRIPTION, value);
    }

}
