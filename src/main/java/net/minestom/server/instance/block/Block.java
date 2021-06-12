package net.minestom.server.instance.block;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Represents a block that can be placed anywhere.
 * Block objects are expected to be reusable and therefore do not
 * retain placement data (e.g. block position)
 * <p>
 * Implementations are expected to be immutable.
 */
public interface Block extends ProtocolObject, TagReadable, BlockConstants {

    <T> @NotNull Block withProperty(@NotNull BlockProperty<T> property, @NotNull T value);

    @NotNull Block withProperty(@NotNull String property, @NotNull String value);

    <T> @NotNull Block withTag(@NotNull Tag<T> tag, @Nullable T value);

    @NotNull Block withNbt(@Nullable NBTCompound compound);

    @NotNull Block withHandler(@Nullable BlockHandler handler);

    <T> @NotNull T getProperty(@NotNull BlockProperty<T> property);

    @NotNull String getProperty(@NotNull String property);

    @Nullable BlockHandler getHandler();

    @NotNull Block getDefaultBlock();

    @NotNull Map<String, String> createPropertiesMap();

    short getStateId();

    default @NotNull Registry.BlockEntry registry() {
        return Registry.block(this);
    }

    default boolean compare(@NotNull Block block, @NotNull Comparator comparator) {
        return comparator.test(this, block);
    }

    default boolean compare(@NotNull Block block) {
        return compare(block, Comparator.ID);
    }

    static @Nullable Block fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return BlockRegistry.fromNamespaceId(namespaceID);
    }

    static @Nullable Block fromNamespaceId(@NotNull String namespaceID) {
        return fromNamespaceId(NamespaceID.from(namespaceID));
    }

    static @Nullable Block fromStateId(short stateId) {
        return BlockRegistry.fromStateId(stateId);
    }

    static @Nullable Block fromBlockId(int blockId) {
        return BlockRegistry.fromBlockId(blockId);
    }

    static void register(@NotNull NamespaceID namespaceID, @NotNull Block block,
                         @NotNull IntRange range,
                         @NotNull Block.Supplier blockSupplier) {
        BlockRegistry.register(namespaceID, block, range, blockSupplier);
    }

    default boolean isAir() {
        return registry().isAir();
    }

    default boolean isSolid() {
        return registry().isSolid();
    }

    default boolean isLiquid() {
        return registry().isLiquid();
    }


    @FunctionalInterface
    interface Comparator extends BiPredicate<Block, Block> {
        Comparator IDENTITY = (b1, b2) -> b1 == b2;

        Comparator ID = (b1, b2) -> b1.getId() == b2.getId();

        Comparator STATE = (b1, b2) -> b1.getStateId() == b2.getStateId();
    }

    @FunctionalInterface
    interface Supplier {
        @NotNull Block get(short stateId);
    }
}
