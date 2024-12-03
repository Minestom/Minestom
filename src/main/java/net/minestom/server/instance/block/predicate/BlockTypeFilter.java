package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public sealed interface BlockTypeFilter extends Predicate<Block> permits BlockTypeFilter.Blocks, BlockTypeFilter.Tag {

    record Blocks(@NotNull List<Block> blocks) implements BlockTypeFilter {
        public Blocks {
            blocks = List.copyOf(blocks);
        }

        public Blocks(@NotNull Block... blocks) {
            this(List.of(blocks));
        }

        @Override
        public boolean test(@NotNull Block block) {
            final int blockId = block.id();
            for (Block b : blocks) {
                if (blockId == b.id()) {
                    return true;
                }
            }
            return false;
        }
    }

    record Tag(@NotNull net.minestom.server.gamedata.tags.Tag tag) implements BlockTypeFilter {
        private static final TagManager TAG_MANAGER = Objects.requireNonNull(MinecraftServer.getTagManager());

        public Tag(@NotNull String namespaceId) {
            this(Objects.requireNonNull(TAG_MANAGER.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, namespaceId),
                    "No such block tag: " + namespaceId));
        }

        @Override
        public boolean test(Block block) {
            return tag.contains(block.namespace());
        }
    }

    NetworkBuffer.Type<BlockTypeFilter> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, BlockTypeFilter value) {
            switch (value) {
                case Blocks blocks -> {
                    buffer.write(NetworkBuffer.VAR_INT, blocks.blocks.size() + 1);
                    for (Block block : blocks.blocks) {
                        buffer.write(NetworkBuffer.VAR_INT, block.id());
                    }
                }
                case Tag tag -> {
                    buffer.write(NetworkBuffer.VAR_INT, 0);
                    buffer.write(NetworkBuffer.STRING, tag.tag.name());
                }
            }
        }

        @Override
        public BlockTypeFilter read(@NotNull NetworkBuffer buffer) {
            final int count = buffer.read(NetworkBuffer.VAR_INT) - 1;
            if (count == -1) {
                return new Tag(buffer.read(NetworkBuffer.STRING));
            } else {
                final List<Block> blocks = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    blocks.add(Block.fromBlockId(buffer.read(NetworkBuffer.VAR_INT)));
                }
                return new Blocks(blocks);
            }
        }
    };

    BinaryTagSerializer<BlockTypeFilter> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull BlockTypeFilter value) {
            return switch (value) {
                case Blocks blocks -> {
                    if (blocks.blocks.size() == 1) {
                        // Special case to match mojang serialization
                        yield StringBinaryTag.stringBinaryTag(blocks.blocks.get(0).name());
                    }

                    ListBinaryTag.Builder<StringBinaryTag> builder = ListBinaryTag.builder(BinaryTagTypes.STRING);
                    for (Block block : blocks.blocks) {
                        builder.add(StringBinaryTag.stringBinaryTag(block.name()));
                    }
                    yield builder.build();
                }
                case Tag tag -> StringBinaryTag.stringBinaryTag("#" + tag.tag.name());
            };
        }

        @Override
        public @NotNull BlockTypeFilter read(@NotNull BinaryTag tag) {
            return switch (tag) {
                case ListBinaryTag list -> {
                    final List<Block> blocks = new ArrayList<>(list.size());
                    for (BinaryTag binaryTag : list) {
                        if (!(binaryTag instanceof StringBinaryTag string)) continue;
                        blocks.add(Objects.requireNonNull(Block.fromNamespaceId(string.value())));
                    }
                    yield new Blocks(blocks);
                }
                case StringBinaryTag string -> {
                    // Could be a tag or a block name depending if it starts with a #
                    final String value = string.value();
                    if (value.startsWith("#")) {
                        yield new Tag(value.substring(1));
                    } else {
                        yield new Blocks(Objects.requireNonNull(Block.fromNamespaceId(value)));
                    }
                }
                default -> throw new IllegalArgumentException("Invalid tag type: " + tag.type());
            };
        }
    };

}
