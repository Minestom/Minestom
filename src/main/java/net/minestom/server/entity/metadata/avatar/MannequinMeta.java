package net.minestom.server.entity.metadata.avatar;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.player.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

public class MannequinMeta extends AvatarMeta {
// ublic static final Entry<ResolvableProfile> PROFILE = index(0, Metadata::ResolvableProfile, ResolvableProfile.EMPTY);
//        public static final Entry<Boolean> IMMOVABLE = index(1, Metadata::Boolean, false);
//        public static final Entry<@Nullable Component> DESCRIPTION = index(2, Metadata::OptComponent, Component.translatable("entity.minecraft.mannequin.label"));
//
    public MannequinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ResolvableProfile getProfile() {
        return metadata.get(MetadataDef.Mannequin.PROFILE);
    }

    public void setProfile(ResolvableProfile value) {
        metadata.set(MetadataDef.Mannequin.PROFILE, value);
    }

    public boolean isImmovable() {
        return metadata.get(MetadataDef.Mannequin.IMMOVABLE);
    }

    public void setImmovable(boolean value) {
        metadata.set(MetadataDef.Mannequin.IMMOVABLE, value);
    }

    public @Nullable Component getDescription() {
        return metadata.get(MetadataDef.Mannequin.DESCRIPTION);
    }

    public void setDescription(@Nullable Component value) {
        metadata.set(MetadataDef.Mannequin.DESCRIPTION, value);
    }

}
