package net.minestom.server.network.player;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record ResolvableProfile(
        Either<GameProfile, Partial> profile,
        PlayerSkin.Patch patch
) {
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
    }
}
