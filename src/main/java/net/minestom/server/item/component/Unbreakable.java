package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record Unbreakable(boolean showInTooltip) {
    public static final Unbreakable DEFAULT = new Unbreakable();

    public static final NetworkBuffer.Type<Unbreakable> NETWORK_TYPE = new NetworkBuffer.Type<Unbreakable>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Unbreakable value) {
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip());
        }

        @Override
        public Unbreakable read(@NotNull NetworkBuffer buffer) {
            return new Unbreakable(buffer.read(NetworkBuffer.BOOLEAN));
        }
    };

    public Unbreakable() {
        this(true);
    }

    public static final BinaryTagSerializer<Unbreakable> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new Unbreakable(tag.getBoolean("showInTooltip", true)),
            unbreakable -> CompoundBinaryTag.builder().putBoolean("showInTooltip", unbreakable.showInTooltip()).build()
    );

}
