package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Block extends Keyed, TagReadable, BlockConstants {

    <T> @NotNull Block withProperty(@NotNull BlockProperty<T> property, @NotNull T value);

    <T> @NotNull Block withTag(@NotNull Tag<T> tag, @Nullable T value);

    @NotNull Block getDefaultBlock();

    @NotNull NamespaceID getNamespaceId();

    @Override
    default @NotNull Key key() {
        return getNamespaceId();
    }

    default @NotNull String getName() {
        return getNamespaceId().asString();
    }

    @NotNull Map<String, String> createPropertiesMap();

    int getBlockId();

    short getStateId();

    @NotNull BlockData getData();

    default boolean compare(@NotNull Block block, @NotNull Comparator comparator) {
        return comparator.equals(this, block);
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

    static void register(@NotNull NamespaceID namespaceID, @NotNull Block block,
                         @NotNull IntRange range,
                         @NotNull Block.Supplier blockSupplier) {
        BlockRegistry.register(namespaceID, block, range, blockSupplier);
    }

    default boolean isSolid() {
        return getData().isSolid();
    }

    default boolean isAir() {
        return getData().isAir();
    }

    @FunctionalInterface
    interface Comparator {
        Comparator IDENTITY = (b1, b2) -> b1 == b2;

        Comparator ID = (b1, b2) -> b1.getBlockId() == b2.getBlockId();

        Comparator STATE = (b1, b2) -> b1.getStateId() == b2.getStateId();

        boolean equals(@NotNull Block b1, @NotNull Block b2);
    }

    @FunctionalInterface
    interface Supplier {
        @NotNull Block get(short stateId);
    }
}
