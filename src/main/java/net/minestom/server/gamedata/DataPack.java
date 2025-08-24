package net.minestom.server.gamedata;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public sealed interface DataPack permits DataPackImpl {

    DataPack MINECRAFT_CORE = new DataPackImpl(Key.key("core"), true);

    /**
     * <p>Returns true if this data pack is synced with the client. The null data pack is never synced.</p>
     *
     * <p>In practice, this currently only makes sense for vanilla and modded content.</p>
     *
     * <p>TODO: in the future this should be based on what the client responds with known packs, I suppose.</p>
     *
     * @return true if this data pack is synced with the client, false otherwise.
     */
    boolean isSynced();

}
