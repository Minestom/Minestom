package net.minestom.server.particle;

import net.kyori.adventure.key.Key;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public sealed interface Particle extends StaticProtocolObject, Particles permits Particle.Block, Particle.BlockMarker,
        Particle.Dust, Particle.DustColorTransition, Particle.DustPillar, Particle.EntityEffect, Particle.FallingDust,
        Particle.Item, Particle.SculkCharge, Particle.Shriek, Particle.Simple, Particle.Vibration, Particle.Trail,
        Particle.BlockCrumble, Particle.TintedLeaves {

    @NotNull NetworkBuffer.Type<Particle> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Particle value) {
            buffer.write(VAR_INT, value.id());
            value.writeData(buffer);
        }

        @Override
        public Particle read(@NotNull NetworkBuffer buffer) {
            final int id = buffer.read(VAR_INT);
            final Particle particle = Objects.requireNonNull(fromId(id), () -> "unknown particle id: " + id);
            return particle.readData(buffer);
        }
    };
    @NotNull Codec<Particle> CODEC = new Codec<>() {
        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Transcoder.MapLike<D>> mapResult = coder.getMap(value);
            if (!(mapResult instanceof Result.Ok(Transcoder.MapLike<D> map)))
                return mapResult.cast();
            final Result<String> typeResult = map.getValue("type").map(coder::getString);
            if (!(typeResult instanceof Result.Ok(String type)))
                return typeResult.cast();
            Particle particle = fromKey(type);
            if (particle == null) return new Result.Error<>("unknown particle type: " + type);
            return particle.decode(coder, map);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Particle value) {
            if (value == null) return new Result.Error<>("null");
            return value.encode(coder);
        }
    };

    static @NotNull Collection<@NotNull Particle> values() {
        return ParticleImpl.values();
    }

    static @Nullable Particle fromKey(@NotNull String key) {
        return ParticleImpl.getSafe(key);
    }

    static @Nullable Particle fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    static @Nullable Particle fromId(int id) {
        return ParticleImpl.getId(id);
    }

    @NotNull Particle readData(@NotNull NetworkBuffer reader);

    void writeData(@NotNull NetworkBuffer writer);

    <D> @NotNull Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map);

    <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder);

    record Simple(@NotNull Key key, int id) implements Particle {

        @Override
        public @NotNull Particle readData(@NotNull NetworkBuffer reader) {
            return this;
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            return new Result.Ok<>(this);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .build());
        }
    }

    record Block(@NotNull Key key, int id,
                 @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull Block withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new Block(key(), id(), block);
        }

        @Override
        public @NotNull Block readData(@NotNull NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            final Result<net.minestom.server.instance.block.Block> blockResult = map.getValue("block_state")
                    .map(blockState -> net.minestom.server.instance.block.Block.CODEC.decode(coder, blockState));
            if (!(blockResult instanceof Result.Ok(net.minestom.server.instance.block.Block block)))
                return blockResult.cast();
            return new Result.Ok<>(this.withBlock(block));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> blockResult = net.minestom.server.instance.block.Block.CODEC.encode(coder, block);
            if (!(blockResult instanceof Result.Ok(D encodedBlock)))
                return blockResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", encodedBlock)
                    .build());
        }
    }

    record BlockMarker(@NotNull Key key, int id,
                       @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull BlockMarker withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new BlockMarker(key(), id(), block);
        }

        @Override
        public @NotNull BlockMarker readData(@NotNull NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            final Result<net.minestom.server.instance.block.Block> blockResult = map.getValue("block_state")
                    .map(blockState -> net.minestom.server.instance.block.Block.CODEC.decode(coder, blockState));
            if (!(blockResult instanceof Result.Ok(net.minestom.server.instance.block.Block block)))
                return blockResult.cast();
            return new Result.Ok<>(this.withBlock(block));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> blockResult = net.minestom.server.instance.block.Block.CODEC.encode(coder, block);
            if (!(blockResult instanceof Result.Ok(D encodedBlock)))
                return blockResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", encodedBlock)
                    .build());
        }
    }

    record Dust(@NotNull Key key, int id, @NotNull RGBLike color, float scale) implements Particle {

        @Contract(pure = true)
        public @NotNull Dust withProperties(@NotNull RGBLike color, float scale) {
            return new Dust(key(), id(), color, scale);
        }

        @Contract(pure = true)
        public @NotNull Dust withColor(@NotNull RGBLike color) {
            return this.withProperties(color, scale);
        }

        @Contract(pure = true)
        public @NotNull Dust withScale(float scale) {
            return this.withProperties(color, scale);
        }

        @Override
        public @NotNull Dust readData(@NotNull NetworkBuffer reader) {
            return this.withProperties(reader.read(Color.NETWORK_TYPE), reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(NetworkBuffer.FLOAT, scale);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            final Result<RGBLike> colorResult = map.getValue("color")
                    .map(color -> Color.CODEC.decode(coder, color));
            if (!(colorResult instanceof Result.Ok(RGBLike color)))
                return colorResult.cast();
            final Result<Float> scaleResult = map.getValue("scale").map(coder::getFloat);
            if (!(scaleResult instanceof Result.Ok(Float scale)))
                return scaleResult.cast();
            return new Result.Ok<>(this.withProperties(color, scale));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder) {
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

    record DustColorTransition(@NotNull Key key, int id, @NotNull RGBLike color,
                               @NotNull RGBLike transitionColor, float scale) implements Particle {

        @Contract(pure = true)
        public @NotNull DustColorTransition withProperties(@NotNull RGBLike color, @NotNull RGBLike transitionColor, float scale) {
            return new DustColorTransition(key, id, color, transitionColor, scale);
        }

        @Contract(pure = true)
        public @NotNull DustColorTransition withColor(@NotNull RGBLike color) {
            return this.withProperties(color, transitionColor, scale);
        }

        @Contract(pure = true)
        public @NotNull DustColorTransition withScale(float scale) {
            return this.withProperties(color, transitionColor, scale);
        }

        @Contract(pure = true)
        public @NotNull DustColorTransition withTransitionColor(@NotNull RGBLike transitionColor) {
            return this.withProperties(color, transitionColor, scale);
        }

        @Override
        public @NotNull DustColorTransition readData(@NotNull NetworkBuffer reader) {
            return this.withProperties(reader.read(Color.NETWORK_TYPE),
                    reader.read(Color.NETWORK_TYPE),
                    reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(Color.NETWORK_TYPE, transitionColor);
            writer.write(NetworkBuffer.FLOAT, scale);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            final Result<RGBLike> fromColorResult = map.getValue("from_color")
                    .map(color -> Color.CODEC.decode(coder, color));
            if (!(fromColorResult instanceof Result.Ok(RGBLike fromColor)))
                return fromColorResult.cast();
            final Result<RGBLike> toColorResult = map.getValue("to_color")
                    .map(color -> Color.CODEC.decode(coder, color));
            if (!(toColorResult instanceof Result.Ok(RGBLike toColor)))
                return toColorResult.cast();
            final Result<Float> scaleResult = map.getValue("scale").map(coder::getFloat);
            if (!(scaleResult instanceof Result.Ok(Float scale)))
                return scaleResult.cast();
            return new Result.Ok<>(this.withProperties(fromColor, toColor, scale));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
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

    record DustPillar(@NotNull Key key, int id,
                      @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull DustPillar withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new DustPillar(key(), id(), block);
        }

        @Override
        public @NotNull DustPillar readData(@NotNull NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            final Result<net.minestom.server.instance.block.Block> blockResult = map.getValue("block_state")
                    .map(blockState -> net.minestom.server.instance.block.Block.CODEC.decode(coder, blockState));
            if (!(blockResult instanceof Result.Ok(net.minestom.server.instance.block.Block block)))
                return blockResult.cast();
            return new Result.Ok<>(this.withBlock(block));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> blockResult = net.minestom.server.instance.block.Block.CODEC.encode(coder, block);
            if (!(blockResult instanceof Result.Ok(D encodedBlock)))
                return blockResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", encodedBlock)
                    .build());
        }
    }

    record FallingDust(@NotNull Key key, int id,
                       @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull FallingDust withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new FallingDust(key(), id(), block);
        }

        @Override
        public @NotNull FallingDust readData(@NotNull NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            final Result<net.minestom.server.instance.block.Block> blockResult = map.getValue("block_state")
                    .map(blockState -> net.minestom.server.instance.block.Block.CODEC.decode(coder, blockState));
            if (!(blockResult instanceof Result.Ok(net.minestom.server.instance.block.Block block)))
                return blockResult.cast();
            return new Result.Ok<>(this.withBlock(block));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> blockResult = net.minestom.server.instance.block.Block.CODEC.encode(coder, block);
            if (!(blockResult instanceof Result.Ok(D encodedBlock)))
                return blockResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", encodedBlock)
                    .build());
        }
    }

    record Item(@NotNull Key key, int id, @NotNull ItemStack item) implements Particle {

        @Contract(pure = true)
        public @NotNull Item withItem(@NotNull ItemStack item) {
            return new Item(key(), id(), item);
        }

        @Override
        public @NotNull Item readData(@NotNull NetworkBuffer reader) {
            return this.withItem(reader.read(ItemStack.NETWORK_TYPE));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(ItemStack.NETWORK_TYPE, item);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull Transcoder.MapLike<D> map) {
            final Result<ItemStack> itemResult = map.getValue("item")
                    .map(item -> ItemStack.CODEC.decode(coder, item));
            if (!(itemResult instanceof Result.Ok(ItemStack item)))
                return itemResult.cast();
            return new Result.Ok<>(this.withItem(item));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> itemResult = ItemStack.CODEC.encode(coder, item);
            if (!(itemResult instanceof Result.Ok(D itemData)))
                return itemResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("item", itemData)
                    .build());
        }
    }

    record EntityEffect(@NotNull Key key, int id, @NotNull AlphaColor color) implements Particle {

        @Contract(pure = true)
        public @NotNull EntityEffect withColor(@NotNull AlphaColor color) {
            return new EntityEffect(key(), id(), color);
        }

        @Contract(pure = true)
        public @NotNull EntityEffect withColor(@NotNull RGBLike color) {
            return new EntityEffect(key(), id(), new AlphaColor(1, color));
        }

        @Contract(pure = true)
        public @NotNull EntityEffect withColor(int alpha, @NotNull RGBLike color) {
            return new EntityEffect(key(), id(), new AlphaColor(alpha, color));
        }

        @Override
        public @NotNull EntityEffect readData(@NotNull NetworkBuffer reader) {
            return withColor(reader.read(AlphaColor.NETWORK_TYPE));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(AlphaColor.NETWORK_TYPE, color);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull Transcoder.MapLike<D> map) {
            final Result<AlphaColor> colorResult = map.getValue("color")
                    .map(color -> AlphaColor.CODEC.decode(coder, color));
            if (!(colorResult instanceof Result.Ok(AlphaColor color)))
                return colorResult.cast();
            return new Result.Ok<>(this.withColor(color));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> colorResult = AlphaColor.CODEC.encode(coder, color);
            if (!(colorResult instanceof Result.Ok(D colorData)))
                return colorResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("color", colorData)
                    .build());
        }
    }

    record SculkCharge(@NotNull Key key, int id, float roll) implements Particle {

        @Contract(pure = true)
        public @NotNull SculkCharge withRoll(float roll) {
            return new SculkCharge(key(), id(), roll);
        }

        @Override
        public @NotNull SculkCharge readData(@NotNull NetworkBuffer reader) {
            return this.withRoll(reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.FLOAT, roll);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull Transcoder.MapLike<D> map) {
            final Result<Float> rollResult = map.getValue("roll").map(coder::getFloat);
            if (!(rollResult instanceof Result.Ok(Float roll)))
                return rollResult.cast();
            return new Result.Ok<>(this.withRoll(roll));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("roll", coder.createFloat(roll))
                    .build());
        }
    }

    record Shriek(@NotNull Key key, int id, int delay) implements Particle {

        @Contract(pure = true)
        public @NotNull Shriek withDelay(int delay) {
            return new Shriek(key(), id(), delay);
        }

        @Override
        public @NotNull Shriek readData(@NotNull NetworkBuffer reader) {
            return this.withDelay(reader.read(NetworkBuffer.VAR_INT));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, delay);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull Transcoder.MapLike<D> map) {
            final Result<Integer> delayResult = map.getValue("delay").map(coder::getInt);
            if (!(delayResult instanceof Result.Ok(Integer delay)))
                return delayResult.cast();
            return new Result.Ok<>(this.withDelay(delay));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("delay", coder.createInt(delay))
                    .build());
        }
    }

    record Vibration(@NotNull Key key, int id, @NotNull SourceType sourceType,
                     @Nullable Point sourceBlockPosition, int sourceEntityId, float sourceEntityEyeHeight,
                     int travelTicks) implements Particle {

        @Contract(pure = true)
        public @NotNull Vibration withProperties(@NotNull SourceType sourceType, @Nullable Point sourceBlockPosition,
                                                 int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
            return new Vibration(key(), id(), sourceType, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Contract(pure = true)
        public @NotNull Vibration withSourceBlockPosition(@Nullable Point sourceBlockPosition, int travelTicks) {
            return new Vibration(key(), id(), SourceType.BLOCK, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Contract(pure = true)
        public @NotNull Vibration withSourceEntity(int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
            return new Vibration(key(), id(), SourceType.ENTITY, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Override
        public @NotNull Vibration readData(@NotNull NetworkBuffer reader) {
            SourceType type = reader.read(NetworkBuffer.Enum(SourceType.class));
            if (type == SourceType.BLOCK) {
                return this.withSourceBlockPosition(reader.read(NetworkBuffer.BLOCK_POSITION), reader.read(NetworkBuffer.VAR_INT));
            } else {
                return this.withSourceEntity(reader.read(NetworkBuffer.VAR_INT), reader.read(NetworkBuffer.FLOAT), reader.read(NetworkBuffer.VAR_INT));
            }
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.Enum(SourceType.class), sourceType);
            if (sourceType == SourceType.BLOCK) {
                Objects.requireNonNull(sourceBlockPosition);
                writer.write(NetworkBuffer.BLOCK_POSITION, sourceBlockPosition);
                writer.write(VAR_INT, travelTicks);
            } else {
                writer.write(VAR_INT, sourceEntityId);
                writer.write(NetworkBuffer.FLOAT, sourceEntityEyeHeight);
                writer.write(VAR_INT, travelTicks);
            }
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull Transcoder.MapLike<D> map) {
            final Result<Transcoder.MapLike<D>> destinationResult = map.getValue("destination").map(coder::getMap);
            if (!(destinationResult instanceof Result.Ok(Transcoder.MapLike<D> destination)))
                return destinationResult.cast();
            final Result<String> typeResult = destination.getValue("type").map(coder::getString);
            if (!(typeResult instanceof Result.Ok(String type)))
                return typeResult.cast();
            if (!type.equals("block"))
                return new Result.Error<>(type + " vibration source cannot be decoded");
            final Result<Point> posResult = destination.getValue("pos")
                    .map(pos -> Codec.BLOCK_POSITION.decode(coder, pos));
            if (!(posResult instanceof Result.Ok(Point position)))
                return posResult.cast();
            final Result<Integer> arrivalResult = map.getValue("arrival_in_ticks").map(coder::getInt);
            if (!(arrivalResult instanceof Result.Ok(Integer travelTicks)))
                return arrivalResult.cast();
            return new Result.Ok<>(this.withSourceBlockPosition(position, travelTicks));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            if (sourceType == SourceType.ENTITY)
                return new Result.Error<>("entity vibration source cannot be encoded");
            final Result<D> posResult = Codec.BLOCK_POSITION.encode(coder, sourceBlockPosition);
            if (!(posResult instanceof Result.Ok(D posData)))
                return posResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("destination", coder.createMap()
                            .put("type", coder.createString("block"))
                            .put("pos", posData)
                            .build())
                    .put("arrival_in_ticks", coder.createInt(travelTicks))
                    .build());
        }

        public enum SourceType {
            BLOCK, ENTITY
        }
    }

    record Trail(@NotNull Key key, int id, @NotNull Point target, @NotNull RGBLike color,
                 int duration) implements Particle {

        public @NotNull Trail withProperties(@NotNull Point target, @NotNull RGBLike color, int duration) {
            return new Trail(key(), id(), target, color, duration);
        }

        public @NotNull Trail withTarget(@NotNull Point target) {
            return new Trail(key(), id(), target, color, duration);
        }

        public @NotNull Trail withColor(@NotNull RGBLike color) {
            return new Trail(key(), id(), target, color, duration);
        }

        public @NotNull Trail withDuration(int duration) {
            return new Trail(key(), id(), target, color, duration);
        }

        @Override
        public @NotNull Trail readData(@NotNull NetworkBuffer reader) {
            return this.withProperties(reader.read(VECTOR3D), reader.read(Color.NETWORK_TYPE), reader.read(VAR_INT));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(VECTOR3D, target);
            writer.write(Color.NETWORK_TYPE, color);
            writer.write(VAR_INT, duration);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull Transcoder.MapLike<D> map) {
            final Result<Point> targetResult = map.getValue("target")
                    .map(target -> Codec.VECTOR3D.decode(coder, target));
            if (!(targetResult instanceof Result.Ok(Point target)))
                return targetResult.cast();
            final Result<RGBLike> colorResult = map.getValue("color")
                    .map(color -> Color.CODEC.decode(coder, color));
            if (!(colorResult instanceof Result.Ok(RGBLike color)))
                return colorResult.cast();
            final Result<Integer> durationResult = map.getValue("duration").map(coder::getInt);
            if (!(durationResult instanceof Result.Ok(Integer duration)))
                return durationResult.cast();
            return new Result.Ok<>(this.withProperties(target, color, duration));
        }

        @Override
        public <D> @NotNull Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> targetResult = Codec.VECTOR3D.encode(coder, target);
            if (!(targetResult instanceof Result.Ok(D targetData)))
                return targetResult.cast();
            final Result<D> colorResult = Color.CODEC.encode(coder, color);
            if (!(colorResult instanceof Result.Ok(D colorData)))
                return colorResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("target", targetData)
                    .put("color", colorData)
                    .put("duration", coder.createInt(duration))
                    .build());
        }
    }

    record BlockCrumble(@NotNull Key key, int id,
                        @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull Block withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new Block(key(), id(), block);
        }

        @Override
        public @NotNull Block readData(@NotNull NetworkBuffer reader) {
            short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
            var block = net.minestom.server.instance.block.Block.fromStateId(blockState);
            Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
            return this.withBlock(block);
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.VAR_INT, block.stateId());
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            final Result<net.minestom.server.instance.block.Block> blockResult = map.getValue("block_state")
                    .map(blockState -> net.minestom.server.instance.block.Block.CODEC.decode(coder, blockState));
            if (!(blockResult instanceof Result.Ok(net.minestom.server.instance.block.Block block)))
                return blockResult.cast();
            return new Result.Ok<>(this.withBlock(block));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
            final Result<D> blockResult = net.minestom.server.instance.block.Block.CODEC.encode(coder, block);
            if (!(blockResult instanceof Result.Ok(D encodedBlock)))
                return blockResult.cast();
            return new Result.Ok<>(coder.createMap()
                    .put("type", coder.createString(key.asString()))
                    .put("block_state", encodedBlock)
                    .build());
        }
    }

    record TintedLeaves(@NotNull Key key, int id, @NotNull AlphaColor color) implements Particle {
        @Contract(pure = true)
        public @NotNull TintedLeaves withColor(@NotNull AlphaColor color) {
            return new TintedLeaves(key(), id(), color);
        }

        @Contract(pure = true)
        public @NotNull TintedLeaves withColor(@NotNull RGBLike color) {
            return new TintedLeaves(key(), id(), new AlphaColor(1, color));
        }

        @Contract(pure = true)
        public @NotNull TintedLeaves withColor(int alpha, @NotNull RGBLike color) {
            return new TintedLeaves(key(), id(), new AlphaColor(alpha, color));
        }

        @Override
        public @NotNull TintedLeaves readData(@NotNull NetworkBuffer reader) {
            return withColor(reader.read(AlphaColor.NETWORK_TYPE));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(AlphaColor.NETWORK_TYPE, color);
        }

        @Override
        public @NotNull <D> Result<Particle> decode(@NotNull Transcoder<D> coder, @NotNull Transcoder.MapLike<D> map) {
            final Result<AlphaColor> colorResult = map.getValue("color")
                    .map(color -> AlphaColor.CODEC.decode(coder, color));
            if (!(colorResult instanceof Result.Ok(AlphaColor color)))
                return colorResult.cast();
            return new Result.Ok<>(this.withColor(color));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder) {
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
