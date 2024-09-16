package net.minestom.server.particle;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public sealed interface Particle extends StaticProtocolObject, Particles permits Particle.Block, Particle.BlockMarker,
        Particle.Dust, Particle.DustColorTransition, Particle.DustPillar, Particle.EntityEffect, Particle.FallingDust,
        Particle.Item, Particle.SculkCharge, Particle.Shriek, Particle.Simple, Particle.Vibration {

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

    static @NotNull Collection<@NotNull Particle> values() {
        return ParticleImpl.values();
    }

    static @Nullable Particle fromNamespaceId(@NotNull String namespaceID) {
        return ParticleImpl.getSafe(namespaceID);
    }

    static @Nullable Particle fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable Particle fromId(int id) {
        return ParticleImpl.getId(id);
    }

    @NotNull Particle readData(@NotNull NetworkBuffer reader);

    void writeData(@NotNull NetworkBuffer writer);

    @NotNull CompoundBinaryTag toNbt();

    record Simple(@NotNull NamespaceID namespace, int id) implements Particle {
        @Override
        public @NotNull Particle readData(@NotNull NetworkBuffer reader) {
            return this;
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .putString("type", namespace.asString())
                    .build();
        }
    }

    record Block(@NotNull NamespaceID namespace, int id, @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull Block withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new Block(namespace(), id(), block);
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
            writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            throw new UnsupportedOperationException("Block particle cannot be serialized to NBT");
        }
    }

    record BlockMarker(@NotNull NamespaceID namespace, int id, @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull BlockMarker withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new BlockMarker(namespace(), id(), block);
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
            writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            throw new UnsupportedOperationException("BlockMarker particle cannot be serialized to NBT");
        }

    }

    record Dust(@NotNull NamespaceID namespace, int id, @NotNull RGBLike color, float scale) implements Particle {

        @Contract (pure = true)
        public @NotNull Dust withProperties(@NotNull RGBLike color, float scale) {
            return new Dust(namespace(), id(), color, scale);
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
            return this.withProperties(new Color(
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255)
            ), reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.FLOAT, color.red() / 255f);
            writer.write(NetworkBuffer.FLOAT, color.green() / 255f);
            writer.write(NetworkBuffer.FLOAT, color.blue() / 255f);
            writer.write(NetworkBuffer.FLOAT, scale);
        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            ListBinaryTag colorTag = ListBinaryTag.builder(BinaryTagTypes.FLOAT)
                    .add(FloatBinaryTag.floatBinaryTag(color.red() / 255f))
                    .add(FloatBinaryTag.floatBinaryTag(color.green() / 255f))
                    .add(FloatBinaryTag.floatBinaryTag(color.blue() / 255f))
                    .build();

            return CompoundBinaryTag.builder()
                    .putString("type", namespace.asString())
                    .put("color", colorTag)
                    .putFloat("scale", scale)
                    .build();
        }
    }

    record DustColorTransition(@NotNull NamespaceID namespace, int id, @NotNull RGBLike color, @NotNull RGBLike transitionColor, float scale) implements Particle {

        @Contract (pure = true)
        public @NotNull DustColorTransition withProperties(@NotNull RGBLike color, @NotNull RGBLike transitionColor, float scale) {
            return new DustColorTransition(namespace, id, color, transitionColor, scale);
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
            return this.withProperties(new Color(
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255)
            ), new Color(
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                    (int) (reader.read(NetworkBuffer.FLOAT) * 255)
            ), reader.read(NetworkBuffer.FLOAT));
        }

        @Override
        public void writeData(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.FLOAT, color.red() / 255f);
            writer.write(NetworkBuffer.FLOAT, color.green() / 255f);
            writer.write(NetworkBuffer.FLOAT, color.blue() / 255f);
            writer.write(NetworkBuffer.FLOAT, transitionColor.red() / 255f);
            writer.write(NetworkBuffer.FLOAT, transitionColor.green() / 255f);
            writer.write(NetworkBuffer.FLOAT, transitionColor.blue() / 255f);
            writer.write(NetworkBuffer.FLOAT, scale);
        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            ListBinaryTag fromColorTag = ListBinaryTag.builder(BinaryTagTypes.FLOAT)
                    .add(FloatBinaryTag.floatBinaryTag(color.red() / 255f))
                    .add(FloatBinaryTag.floatBinaryTag(color.green() / 255f))
                    .add(FloatBinaryTag.floatBinaryTag(color.blue() / 255f))
                    .build();

            ListBinaryTag toColorTag = ListBinaryTag.builder(BinaryTagTypes.FLOAT)
                    .add(FloatBinaryTag.floatBinaryTag(transitionColor.red() / 255f))
                    .add(FloatBinaryTag.floatBinaryTag(transitionColor.green() / 255f))
                    .add(FloatBinaryTag.floatBinaryTag(transitionColor.blue() / 255f))
                    .build();

            return CompoundBinaryTag.builder()
                    .putString("type", namespace.asString())
                    .putFloat("scale", scale)
                    .put("from_color", fromColorTag)
                    .put("to_color", toColorTag)
                    .build();
        }
    }

    record DustPillar(@NotNull NamespaceID namespace, int id, @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull DustPillar withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new DustPillar(namespace(), id(), block);
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
            writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            throw new UnsupportedOperationException("DustPillar particle cannot be serialized to NBT");
        }

    }

    record FallingDust(@NotNull NamespaceID namespace, int id, @NotNull net.minestom.server.instance.block.Block block) implements Particle {

        @Contract(pure = true)
        public @NotNull FallingDust withBlock(@NotNull net.minestom.server.instance.block.Block block) {
            return new FallingDust(namespace(), id(), block);
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
            writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            throw new UnsupportedOperationException("FallingDust particle cannot be serialized to NBT");
        }

    }

    record Item(@NotNull NamespaceID namespace, int id, @NotNull ItemStack item) implements Particle {

        @Contract(pure = true)
        public @NotNull Item withItem(@NotNull ItemStack item) {
            return new Item(namespace(), id(), item);
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
        public @NotNull CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .putString("type", namespace.asString())
                    .put("item", item.toItemNBT())
                    .build();
        }
    }

    record EntityEffect(@NotNull NamespaceID namespace, int id, @NotNull AlphaColor color) implements Particle {

        @Contract(pure = true)
        public @NotNull EntityEffect withColor(@NotNull AlphaColor color) {
            return new EntityEffect(namespace(), id(), color);
        }

        @Contract(pure = true)
        public @NotNull EntityEffect withColor(@NotNull RGBLike color) {
            return new EntityEffect(namespace(), id(), new AlphaColor(1, color));
        }

        @Contract(pure = true)
        public @NotNull EntityEffect withColor(int alpha, @NotNull RGBLike color) {
            return new EntityEffect(namespace(), id(), new AlphaColor(alpha, color));
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
        public @NotNull CompoundBinaryTag toNbt() {
            int color = (0xFF << 24) | (color().red() << 16) | (color().green() << 8) | color().blue();

            return CompoundBinaryTag.builder()
                    .putString("type", namespace.asString())
                    .putInt("color", color)
                    .build();
        }
    }

    record SculkCharge(@NotNull NamespaceID namespace, int id, float roll) implements Particle {

        @Contract(pure = true)
        public @NotNull SculkCharge withRoll(float roll) {
            return new SculkCharge(namespace(), id(), roll);
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
        public @NotNull CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .putString("type", namespace.asString())
                    .putFloat("roll", roll)
                    .build();
        }
    }

    record Shriek(@NotNull NamespaceID namespace, int id, int delay) implements Particle {

        @Contract(pure = true)
        public @NotNull Shriek withDelay(int delay) {
            return new Shriek(namespace(), id(), delay);
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
        public @NotNull CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .putString("type", namespace.asString())
                    .putInt("delay", delay)
                    .build();
        }
    }

    record Vibration(@NotNull NamespaceID namespace, int id, @NotNull SourceType sourceType, @Nullable Point sourceBlockPosition, int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) implements Particle {

        @Contract(pure = true)
        public @NotNull Vibration withProperties(@NotNull SourceType sourceType, @Nullable Point sourceBlockPosition,
                                                         int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
            return new Vibration(namespace(), id(), sourceType, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Contract(pure = true)
        public @NotNull Vibration withSourceBlockPosition(@Nullable Point sourceBlockPosition, int travelTicks) {
            return new Vibration(namespace(), id(), SourceType.BLOCK, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
        }

        @Contract(pure = true)
        public @NotNull Vibration withSourceEntity(int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
            return new Vibration(namespace(), id(), SourceType.ENTITY, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
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
                writer.write(NetworkBuffer.VAR_INT, travelTicks);
            } else {
                writer.write(NetworkBuffer.VAR_INT, sourceEntityId);
                writer.write(NetworkBuffer.FLOAT, sourceEntityEyeHeight);
                writer.write(NetworkBuffer.VAR_INT, travelTicks);
            }

        }

        @Override
        public @NotNull CompoundBinaryTag toNbt() {
            throw new UnsupportedOperationException("Vibration particle cannot be serialized to NBT");
        }

        public enum SourceType {
            BLOCK, ENTITY
        }
    }

}
