package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record RecipeBookAddPacket(@NotNull List<Entry> entries, boolean replace) implements ServerPacket.Play {
    public static final byte FLAG_NOTIFICATION = 1;
    public static final byte FLAG_HIGHLIGHT = 1 << 1;

    public static final NetworkBuffer.Type<RecipeBookAddPacket> SERIALIZER = NetworkBufferTemplate.template(
            Entry.SERIALIZER.list(), RecipeBookAddPacket::entries,
            BOOLEAN, RecipeBookAddPacket::replace,
            RecipeBookAddPacket::new);

    public record Entry(
//            int recipeIndex, Object display,
//            @Nullable Integer group, @NotNull RecipeCategory category,
//            @Nullable List<Recipe.Ingredient> craftingRequirements,
//            byte flags
    ) {
        public static final NetworkBuffer.Type<Entry> SERIALIZER = NetworkBufferTemplate.template(
                Entry::new);

//        public Entry(Object contents, boolean notification, boolean highlight) {
//            this(contents, (byte) ((notification ? FLAG_NOTIFICATION : 0) | (highlight ? FLAG_HIGHLIGHT : 0)));
//        }
//
//        public boolean notification() {
//            return (flags & FLAG_NOTIFICATION) != 0;
//        }
//
//        public boolean highlight() {
//            return (flags & FLAG_HIGHLIGHT) != 0;
//        }
    }

}
