package net.minestom.server.particle;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public sealed interface Particle extends StaticProtocolObject<Particle>, Particles permits Particle.Block, Particle.BlockCrumble, Particle.BlockMarker, Particle.DragonBreath, Particle.Dust, Particle.DustColorTransition, Particle.DustPillar, Particle.Effect, Particle.EntityEffect, Particle.FallingDust, Particle.Flash, Particle.InstantEffect, Particle.Item, Particle.SculkCharge, Particle.Shriek, Particle.Simple, Particle.TintedLeaves, Particle.Trail, Particle.Vibration {

    NetworkBuffer.Type<Particle> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, Particle value) {
            buffer.write(VAR_INT, value.id());
            value.writeData(buffer);
        }

        @Override
        public Particle read(NetworkBuffer buffer) {
            final int id = buffer.read(VAR_INT);
            final Particle particle = Objects.requireNonNull(fromId(id), () -> "unknown particle id: " + id);
            return particle.readData(buffer);
        }
    };
    Codec<Particle> CODEC = new Codec<>() {
        @Override
        public <D> Result<Particle> decode(Transcoder<D> coder, D value) {
            Result<Transcoder.MapLike<D>> mapResult = coder.getMap(value);
            if (!(mapResult instanceof Result.Ok(Transcoder.MapLike<D> map)))
                return mapResult.cast();

            Result<Particle> particleResult = map.getValue("type")
                    .map(coder::getString).mapResult(ParticleImpl::get);
            if (!(particleResult instanceof Result.Ok(Particle particle)))
                return particleResult.cast();

            //noinspection unchecked
            return (Result<Particle>) particle.codec().decodeFromMap(coder, map);
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Particle value) {
            if (value == null) return new Result.Error<>("null");

            //noinspection unchecked
            return ((StructCodec<@NotNull Particle>) value.codec()).encode(coder, value);
        }
    };

    static Collection<Particle> values() {
        return ParticleImpl.REGISTRY.values();
    }

    static @Nullable Particle fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable Particle fromKey(Key key) {
        return ParticleImpl.REGISTRY.get(key);
    }

    static @Nullable Particle fromId(int id) {
        return ParticleImpl.REGISTRY.get(id);
    }

    Particle readData(NetworkBuffer reader);

    void writeData(NetworkBuffer writer);

    StructCodec<? extends Particle> codec();

    record Simple(Key key, int id) implements Particle {
        public static final StructCodec<Simple> CODEC = StructCodec.struct(
                "type", Codec.KEY, Simple::key,
                ParticleImpl::get);

        @Override
        public Particle readData(NetworkBuffer reader) {
            return this;
        }

        @Override
        public void writeData(NetworkBuffer writer) {
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record Block(Key key, int id, net.minestom.server.instance.block.Block block) implements Particle {
        public static final StructCodec<Block> CODEC = StructCodec.struct(
                "type", Codec.KEY, Block::key,
                "block_state", net.minestom.server.instance.block.Block.STATE_CODEC, Block::block,
                (key, block) -> ParticleImpl.<Block>get(key).withBlock(block));

        @Contract(pure = true)
        public Block withBlock(net.minestom.server.instance.block.Block block) {
            return new Block(key(), id(), block);
        }

        @Override
        public Block readData(NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record BlockMarker(Key key, int id, net.minestom.server.instance.block.Block block) implements Particle {
        public static final StructCodec<BlockMarker> CODEC = StructCodec.struct(
                "type", Codec.KEY, BlockMarker::key,
                "block_state", net.minestom.server.instance.block.Block.STATE_CODEC, BlockMarker::block,
                (key, block) -> ParticleImpl.<BlockMarker>get(key).withBlock(block));

        @Contract(pure = true)
        public BlockMarker withBlock(net.minestom.server.instance.block.Block block) {
            return new BlockMarker(key(), id(), block);
        }

        @Override
        public BlockMarker readData(NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record Dust(Key key, int id, RGBLike color, float scale) implements Particle {
        public static final StructCodec<Dust> CODEC = StructCodec.struct(
                "type", Codec.KEY, Dust::key,
                "color", Color.CODEC, Dust::color,
                "scale", Codec.FLOAT, Dust::scale,
                (type, color, scale) -> ParticleImpl.<Dust>get(type).withProperties(color, scale));

        @Contract(pure = true)
        public Dust withProperties(RGBLike color, float scale) {
            return new Dust(key(), id(), color, scale);
        }

        @Contract(pure = true)
        public Dust withColor(RGBLike color) {
            return this.withProperties(color, scale);
        }

        @Contract(pure = true)
        public Dust withScale(float scale) {
            return this.withProperties(color, scale);
        }

        @Override
        public Dust readData(NetworkBuffer reader) {
            return this.withProperties(reader.read(Color.NETWORK_TYPE), reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(NetworkBuffer.FLOAT, scale);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record DustColorTransition(
            Key key, int id,
            RGBLike color,
            RGBLike transitionColor,
            float scale
    ) implements Particle {
        public static final StructCodec<DustColorTransition> CODEC = StructCodec.struct(
                "type", Codec.KEY, DustColorTransition::key,
                "from_color", Color.CODEC, DustColorTransition::color,
                "to_color", Color.CODEC, DustColorTransition::transitionColor,
                "scale", Codec.FLOAT, DustColorTransition::scale,
                (type, from, to, scale) ->
                        ParticleImpl.<DustColorTransition>get(type).withProperties(from, to, scale));

        @Contract(pure = true)
        public DustColorTransition withProperties(RGBLike color, RGBLike transitionColor, float scale) {
            return new DustColorTransition(key, id, color, transitionColor, scale);
        }

        @Contract(pure = true)
        public DustColorTransition withColor(RGBLike color) {
            return this.withProperties(color, transitionColor, scale);
        }

        @Contract(pure = true)
        public DustColorTransition withScale(float scale) {
            return this.withProperties(color, transitionColor, scale);
        }

        @Contract(pure = true)
        public DustColorTransition withTransitionColor(RGBLike transitionColor) {
            return this.withProperties(color, transitionColor, scale);
        }

        @Override
        public DustColorTransition readData(NetworkBuffer reader) {
            return this.withProperties(reader.read(Color.NETWORK_TYPE),
                    reader.read(Color.NETWORK_TYPE),
                    reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(Color.NETWORK_TYPE, transitionColor);
            writer.write(NetworkBuffer.FLOAT, scale);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record DustPillar(Key key, int id, net.minestom.server.instance.block.Block block) implements Particle {
        public static final StructCodec<DustPillar> CODEC = StructCodec.struct(
                "type", Codec.KEY, DustPillar::key,
                "block_state", net.minestom.server.instance.block.Block.STATE_CODEC, DustPillar::block,
                (key, block) -> ParticleImpl.<DustPillar>get(key).withBlock(block));

        @Contract(pure = true)
        public DustPillar withBlock(net.minestom.server.instance.block.Block block) {
            return new DustPillar(key(), id(), block);
        }

        @Override
        public DustPillar readData(NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record FallingDust(Key key, int id, net.minestom.server.instance.block.Block block) implements Particle {
        public static final StructCodec<FallingDust> CODEC = StructCodec.struct(
                "type", Codec.KEY, FallingDust::key,
                "block_state", net.minestom.server.instance.block.Block.STATE_CODEC, FallingDust::block,
                (key, block) -> ParticleImpl.<FallingDust>get(key).withBlock(block));

        @Contract(pure = true)
        public FallingDust withBlock(net.minestom.server.instance.block.Block block) {
            return new FallingDust(key(), id(), block);
        }

        @Override
        public FallingDust readData(NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record Item(Key key, int id, ItemStack item) implements Particle {
        public static final StructCodec<Item> CODEC = StructCodec.struct(
                "type", Codec.KEY, Item::key,
                "item", ItemStack.CODEC, Item::item,
                (type, item) -> ParticleImpl.<Item>get(type).withItem(item));

        @Contract(pure = true)
        public Item withItem(ItemStack item) {
            return new Item(key(), id(), item);
        }

        @Override
        public Item readData(NetworkBuffer reader) {
            return this.withItem(reader.read(ItemStack.NETWORK_TYPE));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(ItemStack.NETWORK_TYPE, item);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record EntityEffect(Key key, int id, AlphaColor color) implements Particle {
        public static final StructCodec<EntityEffect> CODEC = StructCodec.struct(
                "type", Codec.KEY, EntityEffect::key,
                "color", AlphaColor.CODEC, EntityEffect::color,
                (type, color) -> ParticleImpl.<EntityEffect>get(type).withColor(color));

        @Contract(pure = true)
        public EntityEffect withColor(AlphaColor color) {
            return new EntityEffect(key(), id(), color);
        }

        @Contract(pure = true)
        public EntityEffect withColor(RGBLike color) {
            return new EntityEffect(key(), id(), new AlphaColor(1, color));
        }

        @Contract(pure = true)
        public EntityEffect withColor(int alpha, RGBLike color) {
            return new EntityEffect(key(), id(), new AlphaColor(alpha, color));
        }

        @Override
        public EntityEffect readData(NetworkBuffer reader) {
            return withColor(reader.read(AlphaColor.NETWORK_TYPE));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(AlphaColor.NETWORK_TYPE, color);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record SculkCharge(Key key, int id, float roll) implements Particle {
        public static final StructCodec<SculkCharge> CODEC = StructCodec.struct(
                "type", Codec.KEY, SculkCharge::key,
                "roll", Codec.FLOAT, SculkCharge::roll,
                (type, roll) -> ParticleImpl.<SculkCharge>get(type).withRoll(roll));

        @Contract(pure = true)
        public SculkCharge withRoll(float roll) {
            return new SculkCharge(key(), id(), roll);
        }

        @Override
        public SculkCharge readData(NetworkBuffer reader) {
            return this.withRoll(reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.FLOAT, roll);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record Shriek(Key key, int id, int delay) implements Particle {
        public static final StructCodec<Shriek> CODEC = StructCodec.struct(
                "type", Codec.KEY, Shriek::key,
                "delay", Codec.INT, Shriek::delay,
                (type, delay) -> ParticleImpl.<Shriek>get(type).withDelay(delay));

        @Contract(pure = true)
        public Shriek withDelay(int delay) {
            return new Shriek(key(), id(), delay);
        }

        @Override
        public Shriek readData(NetworkBuffer reader) {
            return this.withDelay(reader.read(NetworkBuffer.VAR_INT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, delay);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record Vibration(
            Key key, int id,
            SourceType sourceType,
            @Nullable Point sourceBlockPosition,
            int sourceEntityId,
            float sourceEntityEyeHeight,
            int travelTicks
    ) implements Particle {

        @Contract(pure = true)
        public Vibration withProperties(SourceType sourceType, @Nullable Point sourceBlockPosition,
                                        int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
            return new Vibration(key(), id(), sourceType, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Contract(pure = true)
        public Vibration withSourceBlockPosition(@Nullable Point sourceBlockPosition, int travelTicks) {
            return new Vibration(key(), id(), SourceType.BLOCK, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Contract(pure = true)
        public Vibration withSourceEntity(int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
            return new Vibration(key(), id(), SourceType.ENTITY, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Override
        public Vibration readData(NetworkBuffer reader) {
            SourceType type = reader.read(NetworkBuffer.Enum(SourceType.class));
            if (type == SourceType.BLOCK) {
                return this.withSourceBlockPosition(reader.read(NetworkBuffer.BLOCK_POSITION), reader.read(NetworkBuffer.VAR_INT));
            } else {
                return this.withSourceEntity(reader.read(NetworkBuffer.VAR_INT), reader.read(NetworkBuffer.FLOAT), reader.read(NetworkBuffer.VAR_INT));
            }
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.Enum(SourceType.class), sourceType);
            if (sourceType == SourceType.BLOCK) {
                Objects.requireNonNull(sourceBlockPosition);
                writer.write(NetworkBuffer.BLOCK_POSITION, sourceBlockPosition);
                writer.write(NetworkBuffer.VAR_INT, travelTicks);
            } else {
                writer.write(NetworkBuffer.VAR_INT, sourceEntityId);
                writer.write(NetworkBuffer.FLOAT, sourceEntityEyeHeight);
                writer.write(NetworkBuffer.VAR_INT, travelTicks);
            }
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            throw new UnsupportedOperationException("Vibration particle cannot be serialized to NBT");
        }

        public enum SourceType {
            BLOCK, ENTITY
        }
    }

    record Trail(
            Key key, int id,
            Point target,
            RGBLike color,
            int duration
    ) implements Particle {
        public static final StructCodec<Trail> CODEC = StructCodec.struct(
                "type", Codec.KEY, Trail::key,
                "target", Codec.VECTOR3D, Trail::target,
                "color", Color.CODEC, Trail::color,
                "duration", Codec.INT, Trail::duration,
                (type, target, color, duration) ->
                        ParticleImpl.<Trail>get(type).withProperties(target, color, duration));

        public Trail withProperties(Point target, RGBLike color, int duration) {
            return new Trail(key(), id(), target, color, duration);
        }

        public Trail withTarget(Point target) {
            return new Trail(key(), id(), target, color, duration);
        }

        public Trail withColor(RGBLike color) {
            return new Trail(key(), id(), target, color, duration);
        }

        public Trail withDuration(int duration) {
            return new Trail(key(), id(), target, color, duration);
        }

        @Override
        public Trail readData(NetworkBuffer reader) {
            return this.withProperties(reader.read(VECTOR3D), reader.read(Color.NETWORK_TYPE), reader.read(VAR_INT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(VECTOR3D, target);
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(VAR_INT, duration);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record BlockCrumble(Key key, int id, net.minestom.server.instance.block.Block block) implements Particle {
        public static final StructCodec<BlockCrumble> CODEC = StructCodec.struct(
                "type", Codec.KEY, BlockCrumble::key,
                "block_state", net.minestom.server.instance.block.Block.STATE_CODEC, BlockCrumble::block,
                (key, block) -> ParticleImpl.<BlockCrumble>get(key).withBlock(block));

        @Contract(pure = true)
        public BlockCrumble withBlock(net.minestom.server.instance.block.Block block) {
            return new BlockCrumble(key(), id(), block);
        }

        @Override
        public BlockCrumble readData(NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record TintedLeaves(Key key, int id, AlphaColor color) implements Particle {
        public static final StructCodec<TintedLeaves> CODEC = StructCodec.struct(
                "type", Codec.KEY, TintedLeaves::key,
                "color", AlphaColor.CODEC, TintedLeaves::color,
                (type, color) -> ParticleImpl.<TintedLeaves>get(type).withColor(color));

        @Contract(pure = true)
        public TintedLeaves withColor(AlphaColor color) {
            return new TintedLeaves(key(), id(), color);
        }

        @Contract(pure = true)
        public TintedLeaves withColor(RGBLike color) {
            return new TintedLeaves(key(), id(), new AlphaColor(1, color));
        }

        @Contract(pure = true)
        public TintedLeaves withColor(int alpha, RGBLike color) {
            return new TintedLeaves(key(), id(), new AlphaColor(alpha, color));
        }

        @Override
        public TintedLeaves readData(NetworkBuffer reader) {
            return withColor(reader.read(AlphaColor.NETWORK_TYPE));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(AlphaColor.NETWORK_TYPE, color);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record DragonBreath(Key key, int id, float power) implements Particle {
        public static final StructCodec<DragonBreath> CODEC = StructCodec.struct(
                "type", Codec.KEY, DragonBreath::key,
                "power", Codec.FLOAT, DragonBreath::power,
                (type, power) -> ParticleImpl.<DragonBreath>get(type).withPower(power));

        @Contract(pure = true)
        public DragonBreath withPower(float power) {
            return new DragonBreath(key(), id(), power);
        }

        @Override
        public DragonBreath readData(NetworkBuffer reader) {
            return withPower(reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(NetworkBuffer.FLOAT, power);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record Effect(Key key, int id, RGBLike color, float power) implements Particle {
        public static final StructCodec<Effect> CODEC = StructCodec.struct(
                "type", Codec.KEY, Effect::key,
                "color", Color.CODEC, Effect::color,
                "power", Codec.FLOAT, Effect::power,
                (type, color, power) -> ParticleImpl.<Effect>get(type).withProperties(color, power));

        @Contract(pure = true)
        public Effect withColor(RGBLike color) {
            return new Effect(key(), id(), color, power);
        }

        @Contract(pure = true)
        public Effect withPower(float power) {
            return new Effect(key(), id(), color, power);
        }

        @Contract(pure = true)
        public Effect withProperties(RGBLike color, float power) {
            return new Effect(key(), id(), color, power);
        }

        @Override
        public Effect readData(NetworkBuffer reader) {
            return withProperties(reader.read(Color.NETWORK_TYPE), reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(NetworkBuffer.FLOAT, power);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record Flash(Key key, int id, AlphaColor color) implements Particle {
        public static final StructCodec<Flash> CODEC = StructCodec.struct(
                "type", Codec.KEY, Flash::key,
                "color", Color.CODEC, Flash::color,
                (type, color) -> ParticleImpl.<Flash>get(type).withColor(color));

        @Contract(pure = true)
        public Flash withColor(AlphaColor color) {
            return new Flash(key(), id(), color);
        }

        @Contract(pure = true)
        public Flash withColor(RGBLike color) {
            return new Flash(key(), id(), new AlphaColor(1, color));
        }

        @Contract(pure = true)
        public Flash withColor(int alpha, RGBLike color) {
            return new Flash(key(), id(), new AlphaColor(alpha, color));
        }

        @Override
        public Flash readData(NetworkBuffer reader) {
            return withColor(reader.read(AlphaColor.NETWORK_TYPE));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(AlphaColor.NETWORK_TYPE, color);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

    record InstantEffect(Key key, int id, RGBLike color, float power) implements Particle {
        public static final StructCodec<InstantEffect> CODEC = StructCodec.struct(
                "type", Codec.KEY, InstantEffect::key,
                "color", Color.CODEC, InstantEffect::color,
                "power", Codec.FLOAT, InstantEffect::power,
                (key, color, power) -> ParticleImpl.<InstantEffect>get(key).withProperties(color, power));

        @Contract(pure = true)
        public InstantEffect withColor(RGBLike color) {
            return new InstantEffect(key(), id(), color, power);
        }

        @Contract(pure = true)
        public InstantEffect withPower(float power) {
            return new InstantEffect(key(), id(), color, power);
        }

        @Contract(pure = true)
        public InstantEffect withProperties(RGBLike color, float power) {
            return new InstantEffect(key(), id(), color, power);
        }

        @Override
        public InstantEffect readData(NetworkBuffer reader) {
            return withProperties(reader.read(Color.NETWORK_TYPE), reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(NetworkBuffer writer) {
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(NetworkBuffer.FLOAT, power);
        }

        @Override
        public StructCodec<? extends Particle> codec() {
            return CODEC;
        }
    }

}
