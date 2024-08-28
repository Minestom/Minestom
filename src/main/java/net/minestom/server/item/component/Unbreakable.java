package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;

public record Unbreakable(boolean showInTooltip) {
    public static final Unbreakable DEFAULT = new Unbreakable();

    public static final NetworkBuffer.Type<Unbreakable> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.BOOLEAN, Unbreakable::showInTooltip,
            Unbreakable::new
    );

    public Unbreakable() {
        this(true);
    }

    public static final BinaryTagSerializer<Unbreakable> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new Unbreakable(tag.getBoolean("showInTooltip", true)),
            unbreakable -> CompoundBinaryTag.builder().putBoolean("showInTooltip", unbreakable.showInTooltip()).build()
    );
}
