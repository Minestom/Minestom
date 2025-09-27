package net.minestom.demo.entity;

import net.minestom.server.entity.*;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;

import java.util.ArrayList;
import java.util.Map;

public class PlayerEntity extends Entity {
    private final String username = "Minestom";

    private final String skinTexture = "ewogICJ0aW1lc3RhbXAiIDogMTc1NzcyNDE5NDAzNiwKICAicHJvZmlsZUlkIiA6ICI3MGNkYjNiZjhhN2E0ODYxYWY0ZWEzY2U1MDcwY2ViOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lc3RvbSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iMTNkOTU0ZjRjMGJiMzYyY2MxNzYxYWRhYWY5N2NjYzcxNWM1OTc5MDdkNjdlNDI5ZmQzNzM3N2FiZWYwNTkyIgogICAgfQogIH0KfQ==";
    private final String skinSignature = "TaCq03pmLJthNtgsp7s2LnNgMLnOyy2B4GlGC7stxQ0O5nH42Qp7z7MBVa2hCJVPTje4bjWIoaG4Ydsw/RrE1MZ+XP9EgN5ZYipeoeHqk21uYlGWzlTqTvMxjExHmoFZFvyhWSfaqp6fM69l+Da9pf2gNeqPLg8J8GdQCBHhGbJFSP9f/Cb/fTp9/Y69eXhxP9TBLA1Eqo9bnYJzYBqIYXkbeyPZDJnZ1wOhnlZCNzo/276qsPIMMkSYRI0x4AAs0NfadbdAyszb8txW3eULQHlh2kOgColzC3Mldj3ad2/UXF4tsEJX6YcEmrbJU2lbanhVUCECKkyZxvOVGe8D6ImOyPGuGouAMBEDwWpa8gQTV96czDe0CJbGbT0l84G0mB5N8v1h1j8/7bupQ4i3S9aUHYyl0g+EtFGuUIMk8DdiAGNMJNi21zSm/oZ8W7fYO1rg8MTggHebzGvRUWY1hF9NcjHl5bBoT1VzW5EM8LFqOZAwRXWIZEZuMwaXhlhNGyDg/zV9eGUmhykiLTWvh0iA/OK7dtKqPo4XxewDIPEjpFo7Q0q1hGkHlhuA2u9+0amHPW/TyQOT2KbK+1fW63nAT8N8KfXLBSVC/5NOB+KEBzBbIY2vqTJW3p9+I4dZQ8hDCF8WCu3zD9zal4u3A8aGQpcMLzOG+BrghNPZz/A=";

    public PlayerEntity() {
        super(EntityType.PLAYER);

        setNoGravity(true);
    }

    @Override
    public void updateNewViewer(Player player) {
        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
        properties.add(new PlayerInfoUpdatePacket.Property("textures", skinTexture, skinSignature));
        var entry = new PlayerInfoUpdatePacket.Entry(
                getUuid(), username, properties, false,
                0, GameMode.SURVIVAL, null,
                null, 0, true);
        player.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry));

        // Spawn the player entity
        super.updateNewViewer(player);

        // Enable skin layers
        player.sendPackets(new EntityMetaDataPacket(getEntityId(), Map.of(17, Metadata.Byte((byte) 127))));
    }

    @Override
    public void updateOldViewer(Player player) {
        super.updateOldViewer(player);

        player.sendPacket(new PlayerInfoRemovePacket(getUuid()));
    }
}
