package net.minestom.server.instance.block.predicate;

import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            return tag.contains(block.key());
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
    Codec<BlockTypeFilter> CODEC = new Codec<>() {
        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable BlockTypeFilter value) {
            return switch (value) {
                case Blocks blocks -> {
                    if (blocks.blocks.size() == 1) {
                        // Special case to match mojang serialization
                        yield new Result.Ok<>(coder.createString(blocks.blocks.getFirst().name()));
                    }

                    final Transcoder.ListBuilder<D> list = coder.createList(blocks.blocks.size());
                    for (Block block : blocks.blocks) {
                        list.add(coder.createString(block.name()));
                    }
                    yield new Result.Ok<>(list.build());
                }
                case Tag tag -> new Result.Ok<>(coder.createString("#" + tag.tag.name()));
            };
        }

        @Override
        public @NotNull <D> Result<BlockTypeFilter> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<String> stringResult = coder.getString(value);
            if (stringResult instanceof Result.Ok(String string)) {
                // Could be a tag or a block name depending if it starts with a #
                if (string.startsWith("#")) {
                    return new Result.Ok<>(new Tag(string.substring(1)));
                } else {
                    return new Result.Ok<>(new Blocks(Objects.requireNonNull(Block.fromKey(string))));
                }
            }
            final Result<Integer> listSizeResult = coder.listSize(value);
            if (!(listSizeResult instanceof Result.Ok(Integer listSize)))
                return listSizeResult.cast();

            final List<Block> blocks = new ArrayList<>(listSize);
            for (int i = 0; i < listSize; i++) {
                final Result<D> indexResult = coder.getIndex(value, i);
                if (!(indexResult instanceof Result.Ok(D indexValue)))
                    return indexResult.cast();
                final Result<String> itemResult = coder.getString(indexValue);
                if (!(itemResult instanceof Result.Ok(String item)))
                    return itemResult.cast();
                blocks.add(Objects.requireNonNull(Block.fromKey(item)));
            }
            return new Result.Ok<>(new Blocks(blocks));
        }
    };

}
