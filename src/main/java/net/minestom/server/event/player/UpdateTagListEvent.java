package net.minestom.server.event.player;

import net.minestom.server.event.Event;
import net.minestom.server.network.packet.server.play.TagsPacket;

public class UpdateTagListEvent extends Event {

    private TagsPacket packet;

    public UpdateTagListEvent(TagsPacket packet) {
        this.packet = packet;
    }

    public TagsPacket getTags() {
        return packet;
    }
}
