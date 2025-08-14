package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;
import net.minestom.server.tag.Tag;

import java.util.Collection;
import java.util.List;

public class SignHandler implements BlockHandler {
    @Override
    public Key getKey() {
        return Key.key("minestom:sign");
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        interaction.getPlayer().sendPacket(
                new OpenSignEditorPacket(
                        interaction.getBlockPosition(),
                        true
                )
        );

        return true;
    }

    @Override
    public Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.NBT("front_text"),
                Tag.NBT("back_text"),
                Tag.Boolean("is_waxed")
        );
    }
}
