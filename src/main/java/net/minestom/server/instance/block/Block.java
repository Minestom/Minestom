package net.minestom.server.instance.block;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.kyori.adventure.translation.Translatable;
import net.minestom.server.coordinate.Area;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.Batch;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Represents a block that can be placed anywhere.
 * Block objects are expected to be reusable and therefore do not
 * retain placement data (e.g. block position)
 * <p>
 * Implementations are expected to be immutable.
 */
public sealed interface Block extends StaticProtocolObject<Block>, TagReadable, Blocks, Translatable permits BlockImpl {

    NetworkBuffer.Type<Block> ID_NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(Block::fromBlockId, Block::id);
    NetworkBuffer.Type<Block> STATE_NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(Block::fromStateId, Block::stateId);

    /**
     * Codec for blocks states as strings.
     * Format: <code>"minecraft:x[a=y,b=z]"</code>
     */
    Codec<Block> STATE_CODEC = Codec.STRING.transform(state -> Objects.requireNonNull(
            Block.fromState(state), () -> "not a block state: " + state), Block::state);

    /**
     * Codec for block states as a map.
     * Format: <code>{Name:"minecraft:x",Properties:{a:"y",b:"z"}}</code>
     */
    Codec<Block> STATE_STRUCT_CODEC = new StructCodec<>() {
        @Override
        public <D> Result<Block> decodeFromMap(Transcoder<D> coder, Transcoder.MapLike<D> map) {
            Result<Block> blockResult = map.getValue("Name").map(coder::getString).mapResult(Block::fromKey);
            if (!(blockResult instanceof Result.Ok(Block block)))
                return blockResult.cast();
            Result<Transcoder.MapLike<D>> propertiesResult = map.getValue("Properties").map(coder::getMap);
            if (!(propertiesResult instanceof Result.Ok(Transcoder.MapLike<D> properties)))
                // properties are optional
                return new Result.Ok<>(block);
            for (String key : properties.keys()) {
                Result<String> valueResult = properties.getValue(key).map(coder::getString);
                if (!(valueResult instanceof Result.Ok(String mapValue))) {
                    return new Result.Error<>("No string value found for property " + key + " in block state");
                }
                block = block.withProperty(key, mapValue);
            }
            return new Result.Ok<>(block);
        }

        @Override
        public <D> Result<D> encodeToMap(Transcoder<D> coder, Block value, Transcoder.MapBuilder<D> map) {
            if (value == null) return new Result.Error<>("null");
            map.put("Name", coder.createString(value.key().asMinimalString()));
            if (value.properties().isEmpty()) {
                return new Result.Ok<>(map.build());
            }
            Map<String, String> defaultProperties = value.defaultState().properties();
            Transcoder.MapBuilder<D> propertiesBuilder = coder.createMap();
            boolean nonDefaultPropertyExists = false;
            for (Map.Entry<String, String> entry : value.properties().entrySet()) {
                if (defaultProperties.getOrDefault(entry.getKey(), "").equals(entry.getValue()))
                    continue; // Skip default values
                propertiesBuilder.put(entry.getKey(), coder.createString(entry.getValue()));
                nonDefaultPropertyExists = true;
            }
            if (nonDefaultPropertyExists) {
                map.put("Properties", propertiesBuilder.build());
            }
            return new Result.Ok<>(map.build());
        }
    };

    /**
     * Creates a new block with the the property {@code property} sets to {@code value}.
     *
     * @param property the property name
     * @param value    the property value
     * @return a new block with its property changed
     * @throws IllegalArgumentException if the property or value are invalid
     */
    @Contract(pure = true)
    Block withProperty(String property, String value);

    /**
     * Changes multiple properties at once.
     * <p>
     * Equivalent to calling {@link #withProperty(String, String)} for each map entry.
     *
     * @param properties map containing all the properties to change
     * @return a new block with its properties changed
     * @throws IllegalArgumentException if the property or value are invalid
     * @see #withProperty(String, String)
     */
    @Contract(pure = true)
    Block withProperties(Map<String, String> properties);

    /**
     * Creates a new block with a tag modified.
     *
     * @param tag   the tag to modify
     * @param value the tag value, null to remove
     * @param <T>   the tag type
     * @return a new block with the modified tag
     */
    @Contract(pure = true)
    <T> Block withTag(Tag<T> tag, @Nullable T value);

    /**
     * Creates a new block with different nbt data.
     *
     * @param compound the new block nbt, null to remove
     * @return a new block with different nbt
     */
    @Contract(pure = true)
    Block withNbt(@Nullable CompoundBinaryTag compound);

    /**
     * Creates a new block with the specified {@link BlockHandler handler}.
     *
     * @param handler the new block handler, null to remove
     * @return a new block with the specified handler
     */
    @Contract(pure = true)
    Block withHandler(@Nullable BlockHandler handler);

    /**
     * Returns an unmodifiable view to the block nbt.
     * <p>
     * Be aware that {@link Tag tags} directly affect the block nbt.
     *
     * @return the block nbt, null if not present
     */
    @Contract(pure = true)
    @Nullable CompoundBinaryTag nbt();

    /**
     * Returns an unmodifiable view of the block nbt or an empty compound.
     *
     * @return the block nbt or an empty compound if not present
     */
    default CompoundBinaryTag nbtOrEmpty() {
        return Objects.requireNonNullElse(nbt(), CompoundBinaryTag.empty());
    }

    @Contract(pure = true)
    default boolean hasNbt() {
        return nbt() != null;
    }

    /**
     * Returns the block handler.
     *
     * @return the block handler, null if not present
     */
    @Contract(pure = true)
    @Nullable BlockHandler handler();

    /**
     * Returns the block properties.
     *
     * @return the block properties map
     */
    @Unmodifiable
    @Contract(pure = true)
    Map<String, String> properties();

    /**
     * Returns the block states as a string.
     * <p>
     * The format is `block_name[property1=value1,property2=value2,...]`.
     * <p>
     * More portable than {@link #stateId()} across game versions, but less efficient.
     * Do not rely on exact string comparison as properties order may vary, use {@link #fromState(String)}.
     *
     * @return the block properties as a string
     * @see #fromState(String)
     */
    @Contract(pure = true)
    String state();

    /**
     * Returns this block type with default properties, no tags and no handler.
     * As found in the {@link Blocks} listing.
     *
     * @return the default block
     */
    @Contract(pure = true)
    Block defaultState();

    /**
     * Returns a property value from {@link #properties()}.
     *
     * @param property the property name
     * @return the property value, null if not present (due to an invalid property name)
     */
    @Contract(pure = true)
    @Nullable String getProperty(String property);

    @Contract(pure = true)
    Collection<Block> possibleStates();

    /**
     * Returns the block registry.
     * <p>
     * Registry data is directly linked to {@link #stateId()}.
     *
     * @return the block registry
     */
    @Contract(pure = true)
    RegistryData.BlockEntry registry();

    @Override
    default Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    default int stateId() {
        return registry().stateId();
    }

    default boolean isAir() {
        return registry().isAir();
    }

    default boolean isSolid() {
        return registry().isSolid();
    }

    /** Whether this block stops entity movement (motion-blocking collision); unlike {@link #isSolid()}, e.g. cobweb is solid but does not block motion. */
    default boolean blocksMotion() {
        return registry().blocksMotion();
    }

    default boolean isLiquid() {
        return registry().isLiquid();
    }

    default boolean isFluid() {
        return registry().isFluid();
    }

    @Override
    default String translationKey() {
        return registry().translationKey();
    }

    default boolean compare(Block block, Comparator comparator) {
        return comparator.test(this, block);
    }

    default boolean compare(Block block) {
        return compare(block, Comparator.ID);
    }

    static Collection<Block> values() {
        return BlockImpl.REGISTRY.values();
    }

    static @Nullable Block fromKey(@KeyPattern String key) {
        try {
            return fromKey(Key.key(key));
        } catch (InvalidKeyException e) {
            return null;
        }
    }

    static @Nullable Block fromKey(Key key) {
        return BlockImpl.REGISTRY.get(key);
    }

    static @Nullable Block fromState(String state) {
        return BlockImpl.parseState(state);
    }

    static int statesCount() {
        return BlockImpl.statesCount();
    }

    static @Nullable Block fromStateId(int stateId) {
        return BlockImpl.getState(stateId);
    }

    static @Nullable Block fromBlockId(int blockId) {
        return BlockImpl.REGISTRY.get(blockId);
    }

    static Registry<Block> staticRegistry() {
        return BlockImpl.REGISTRY;
    }

    @FunctionalInterface
    interface Comparator extends BiPredicate<Block, Block> {
        Comparator IDENTITY = (b1, b2) -> b1 == b2;

        Comparator ID = (b1, b2) -> b1.id() == b2.id();

        Comparator STATE = (b1, b2) -> b1.stateId() == b2.stateId();
    }

    /**
     * Represents an element which can place blocks at position.
     * <p>
     * Notably used by {@link Instance}, {@link Batch}.
     */
    interface Setter {
        void setBlock(int x, int y, int z, Block block);

        default void setBlock(Point blockPosition, Block block) {
            setBlock(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), block);
        }

        default void setBlockArea(Area area, Block block) {
            for (BlockVec vec : area) setBlock(vec.blockX(), vec.blockY(), vec.blockZ(), block);
        }
    }

    interface Getter {
        @UnknownNullability
        Block getBlock(int x, int y, int z, Condition condition);

        default @UnknownNullability Block getBlock(Point point, Condition condition) {
            return getBlock(point.blockX(), point.blockY(), point.blockZ(), condition);
        }

        default Block getBlock(int x, int y, int z) {
            return Objects.requireNonNull(getBlock(x, y, z, Condition.NONE));
        }

        default Block getBlock(Point point) {
            return Objects.requireNonNull(getBlock(point, Condition.NONE));
        }

        /**
         * Represents a hint to retrieve blocks more efficiently.
         * Implementing interfaces do not have to honor this.
         */
        enum Condition {
            /**
             * Returns a block no matter what.
             * {@link Block#AIR} being the default result.
             */
            NONE,
            /**
             * Hints that the method should return only if the block is cached.
             * <p>
             * Useful if you are only interested in a block handler or nbt.
             */
            CACHED,
            /**
             * Hints that we only care about the block type.
             * <p>
             * Useful if you need to retrieve registry information about the block.
             * Be aware that the returned block may not return the proper handler/nbt.
             */
            TYPE
        }
    }
}
