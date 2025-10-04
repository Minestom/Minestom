package net.minestom.server.network.player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ResolvableProfile(
        Either<GameProfile, Partial> profile,
        PlayerSkin.Patch patch
) implements PlayerHeadObjectContents.SkinSource {
    public static final ResolvableProfile EMPTY = new ResolvableProfile(Either.right(Partial.EMPTY), PlayerSkin.Patch.EMPTY);

    public static final NetworkBuffer.Type<ResolvableProfile> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.Either(GameProfile.SERIALIZER, Partial.NETWORK_TYPE), ResolvableProfile::profile,
            PlayerSkin.Patch.NETWORK_TYPE, ResolvableProfile::patch,
            ResolvableProfile::new);
    public static final StructCodec<ResolvableProfile> CODEC = StructCodec.struct(
            StructCodec.INLINE, Codec.EitherStruct(GameProfile.CODEC, Partial.CODEC), ResolvableProfile::profile,
            StructCodec.INLINE, PlayerSkin.Patch.CODEC, ResolvableProfile::patch,
            ResolvableProfile::new);

    public ResolvableProfile(GameProfile profile) {
        this(Either.left(profile), PlayerSkin.Patch.EMPTY);
    }

    public ResolvableProfile(GameProfile profile, PlayerSkin.Patch patch) {
        this(Either.left(profile), patch);
    }

    public ResolvableProfile(Partial partial) {
        this(Either.right(partial), PlayerSkin.Patch.EMPTY);
    }

    public ResolvableProfile(Partial partial, PlayerSkin.Patch patch) {
        this(Either.right(partial), patch);
    }

    public ResolvableProfile(PlayerSkin skin) {
        this(new Partial(null, null, List.of(
                new GameProfile.Property("textures", skin.textures(), skin.signature())
        )));
    }

    public record Partial(
            @Nullable String name,
            @Nullable UUID uuid,
            List<GameProfile.Property> properties
    ) {
        public static final Partial EMPTY = new Partial(null, null, List.of());

        public static final NetworkBuffer.Type<Partial> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.STRING.optional(), Partial::name,
                NetworkBuffer.UUID.optional(), Partial::uuid,
                GameProfile.Property.SERIALIZER.list(GameProfile.MAX_PROPERTIES), Partial::properties,
                Partial::new);
        public static final StructCodec<Partial> CODEC = StructCodec.struct(
                "name", Codec.STRING.optional(), Partial::name,
                "uuid", Codec.UUID.optional(), Partial::uuid,
                "properties", GameProfile.Property.LIST_CODEC, Partial::properties,
                Partial::new);

        public Partial {
            properties = List.copyOf(properties);
        }
    }

    // Adventure Mapping

    public static ResolvableProfile fromPlayerHeadContents(PlayerHeadObjectContents contents) {
        final Key texture = contents.texture();
        if (texture != null) return new ResolvableProfile(Partial.EMPTY, new PlayerSkin.Patch(texture));

        final List<GameProfile.Property> properties = new ArrayList<>(contents.profileProperties().size());
        for (PlayerHeadObjectContents.ProfileProperty property : contents.profileProperties()) {
            properties.add(property instanceof GameProfile.Property p ? p :
                    new GameProfile.Property(property.name(), property.value(), property.signature()));
        }
        return new ResolvableProfile(new Partial(contents.name(), contents.id(), properties));
    }

    @Override
    @SuppressWarnings("UnstableApiUsage") // Its a platform API, we are allowed to implement it.
    public void applySkinToPlayerHeadContents(PlayerHeadObjectContents.Builder builder) {
        if (patch.body() != null) builder.texture(patch.body());
        switch (profile) {
            case Either.Left(GameProfile gameProfile) -> {
                builder.name(gameProfile.name());
                builder.id(gameProfile.uuid());
                for (GameProfile.Property property : gameProfile.properties())
                    builder.profileProperty(property);
            }
            case Either.Right(Partial partial) -> {
                builder.name(partial.name());
                builder.id(partial.uuid());
                for (GameProfile.Property property : partial.properties())
                    builder.profileProperty(property);
            }
        }
    }
}
