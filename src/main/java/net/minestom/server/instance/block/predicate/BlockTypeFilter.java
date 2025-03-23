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
                return new Result.Ok<>(string.startsWith("#")
                        ? new Tag(string.substring(1))
                        : new Blocks(Objects.requireNonNull(Block.fromKey(string))));
            }

            // Otherwise, must be a list of blocks
            final Result<List<D>> listResult = coder.getList(value);
            if (!(listResult instanceof Result.Ok(List<D> list)))
                return listResult.cast();
            
            final List<Block> blocks = new ArrayList<>(list.size());
            for (final D entry : list) {
                final Result<String> itemResult = coder.getString(entry);
                if (!(itemResult instanceof Result.Ok(String item)))
                    return itemResult.cast();

                blocks.add(Objects.requireNonNull(Block.fromKey(item)));
            }
            return new Result.Ok<>(new Blocks(blocks));
        }
    };

}
