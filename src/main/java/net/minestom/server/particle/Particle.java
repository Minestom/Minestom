package net.minestom.server.particle;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public sealed interface Particle extends StaticProtocolObject<Particle>, Particles permits Particle.Block, Particle.BlockMarker,
        Particle.Dust, Particle.DustColorTransition, Particle.DustPillar, Particle.EntityEffect, Particle.FallingDust,
        Particle.Item, Particle.SculkCharge, Particle.Shriek, Particle.Simple, Particle.Vibration, Particle.Trail,
        Particle.BlockCrumble, Particle.TintedLeaves {

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
            return new Result.Error<>("particles cannot be decoded");
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable Particle value) {
            if (value == null) return new Result.Error<>("null");
            return value.encode(coder);
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

    <D> Result<D> encode(Transcoder<D> coder);

    record Simple(Key key, int id) implements Particle {

        @Override
        public Particle readData(NetworkBuffer reader) {
            return this;
        }

        @Override
        public void writeData(NetworkBuffer writer) {
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .build());
        }
    }

    record Block(Key key, int id,
                 net.minestom.server.instance.block.Block block) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", coder.createString(block.state()))
                    .build());
        }
    }

    record BlockMarker(Key key, int id,
                       net.minestom.server.instance.block.Block block) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", coder.createString(block.state()))
                    .build());
        }
    }

    record Dust(Key key, int id, RGBLike color, float scale) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            final Result<D> colorResult = Color.CODEC.encode(coder, color);
            if (!(colorResult instanceof Result.Ok(D colorData)))
                return colorResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("color", colorData)
                    .put("scale", coder.createFloat(scale))
                    .build());
        }
    }

    record DustColorTransition(Key key, int id, RGBLike color,
                               RGBLike transitionColor, float scale) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            final Result<D> fromColorResult = Color.CODEC.encode(coder, color);
            if (!(fromColorResult instanceof Result.Ok(D fromColorData)))
                return fromColorResult.cast();
            final Result<D> toColorResult = Color.CODEC.encode(coder, transitionColor);
            if (!(toColorResult instanceof Result.Ok(D toColorData)))
                return toColorResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("from_color", fromColorData)
                    .put("to_color", toColorData)
                    .put("scale", coder.createFloat(scale))
                    .build());
        }
    }

    record DustPillar(Key key, int id,
                      net.minestom.server.instance.block.Block block) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", coder.createString(block.state()))
                    .build());
        }
    }

    record FallingDust(Key key, int id,
                       net.minestom.server.instance.block.Block block) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", coder.createString(block.state()))
                    .build());
        }
    }

    record Item(Key key, int id, ItemStack item) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            final Result<D> itemResult = ItemStack.CODEC.encode(coder, item);
            if (!(itemResult instanceof Result.Ok(D itemData)))
                return itemResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("item", itemData)
                    .build());
        }
    }

    record EntityEffect(Key key, int id, AlphaColor color) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            final Result<D> colorResult = AlphaColor.CODEC.encode(coder, color);
            if (!(colorResult instanceof Result.Ok(D colorData)))
                return colorResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("color", colorData)
                    .build());
        }
    }

    record SculkCharge(Key key, int id, float roll) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("roll", coder.createFloat(roll))
                    .build());
        }
    }

    record Shriek(Key key, int id, int delay) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("delay", coder.createInt(delay))
                    .build());
        }
    }

    record Vibration(Key key, int id, SourceType sourceType,
                     @Nullable Point sourceBlockPosition, int sourceEntityId, float sourceEntityEyeHeight,
                     int travelTicks) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Error<>("Vibration particle cannot be serialized to NBT");
        }

        public enum SourceType {
            BLOCK, ENTITY
        }
    }

    record Trail(Key key, int id, Point target, RGBLike color,
                 int duration) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            final Result<D> colorResult = Color.CODEC.encode(coder, color);
            if (!(colorResult instanceof Result.Ok(D colorData)))
                return colorResult.cast();
            final Result<D> targetResult = Codec.VECTOR3D.encode(coder, target);
            if (!(targetResult instanceof Result.Ok(D targetData)))
                return targetResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("target", targetData)
                    .put("color", colorData)
                    .build());
        }
    }

    record BlockCrumble(Key key, int id,
                        net.minestom.server.instance.block.Block block) implements Particle {

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
        public <D> Result<D> encode(Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", coder.createString(block.state()))
                    .build());
        }
    }

    record TintedLeaves(Key key, int id, AlphaColor color) implements Particle {
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
        public <D> Result<D> encode(Transcoder<D> coder) {
            final Result<D> colorResult = AlphaColor.CODEC.encode(coder, color);
            if (!(colorResult instanceof Result.Ok(D colorData)))
                return colorResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("color", colorData)
                    .build());
        }
    }

}
