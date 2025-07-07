package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SignHandler implements BlockHandler {
    @Override
    public @NotNull Key getKey() {
        return Key.key("minestom:sign");
    }

    @Override
    public boolean onInteract(@NotNull BlockChange.Player interaction) {
        interaction.player().sendPacket(
                new OpenSignEditorPacket(
                        interaction.blockPosition(),
                        true
                )
        );

        return true;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.NBT("front_text"),
                Tag.NBT("back_text"),
                Tag.Boolean("is_waxed")
        );
    }
}
