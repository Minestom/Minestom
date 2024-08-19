package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record JukeboxPlayable(@NotNull DynamicRegistry.Key<JukeboxSong> song, boolean showInTooltip) {
    public static final NetworkBuffer.Type<JukeboxPlayable> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        // For some reason I(matt) cannot discern, the wire format for this type can write the song
        // as either a registry ID or a namespace ID. Minestom always writes as a registry id.

        @Override
        public void write(@NotNull NetworkBuffer buffer, JukeboxPlayable value) {
            buffer.write(NetworkBuffer.BOOLEAN, true); // First option (registry id)
            buffer.write(JukeboxSong.NETWORK_TYPE, value.song);
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip);
        }

        @Override
        public JukeboxPlayable read(@NotNull NetworkBuffer buffer) {
            DynamicRegistry.Key<JukeboxSong> song;
            if (buffer.read(NetworkBuffer.BOOLEAN)) {
                song = buffer.read(JukeboxSong.NETWORK_TYPE);
            } else {
                song = DynamicRegistry.Key.of(buffer.read(NetworkBuffer.STRING));
                final DynamicRegistry<JukeboxSong> registry = MinecraftServer.getJukeboxSongRegistry();
                Check.stateCondition(registry.get(song) != null, "unknown song: {0}", song);
            }
            return new JukeboxPlayable(song, buffer.read(NetworkBuffer.BOOLEAN));
        }
    };
    public static final BinaryTagSerializer<JukeboxPlayable> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull JukeboxPlayable value) {
            return CompoundBinaryTag.builder()
                    .put("song", JukeboxSong.NBT_TYPE.write(context, value.song))
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build();
        }

        @Override
        public @NotNull JukeboxPlayable read(@NotNull Context context, @NotNull BinaryTag raw) {
            if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("expected compound tag");
            return new JukeboxPlayable(
                    JukeboxSong.NBT_TYPE.read(context, tag.get("song")),
                    tag.getBoolean("show_in_tooltip")
            );
        }
    };

    public JukeboxPlayable(@NotNull DynamicRegistry.Key<JukeboxSong> song) {
        this(song, true);
    }

    public @NotNull JukeboxPlayable withSong(@NotNull DynamicRegistry.Key<JukeboxSong> song) {
        return new JukeboxPlayable(song, showInTooltip);
    }

    public @NotNull JukeboxPlayable withTooltip(boolean showInTooltip) {
        return new JukeboxPlayable(song, showInTooltip);
    }

}
