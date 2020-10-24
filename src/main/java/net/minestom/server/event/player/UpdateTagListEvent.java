package net.minestom.server.event.player;

import net.minestom.server.event.Event;
import net.minestom.server.network.packet.server.play.TagsPacket;
import org.jetbrains.annotations.NotNull;

public class UpdateTagListEvent extends Event {

    private TagsPacket packet;

    public UpdateTagListEvent(@NotNull TagsPacket packet) {
        this.packet = packet;
    }

    @NotNull
    public TagsPacket getTags() {
        return packet;
    }
}
