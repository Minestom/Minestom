package net.minestom.server.entity.metadata.minecart;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class CommandBlockMinecartMeta extends AbstractMinecartMeta {
    public CommandBlockMinecartMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull String getCommand() {
        return metadata.get(MetadataDef.MinecartCommandBlock.COMMAND);
    }

    public void setCommand(@NotNull String value) {
        metadata.set(MetadataDef.MinecartCommandBlock.COMMAND, value);
    }

    public @NotNull Component getLastOutput() {
        return metadata.get(MetadataDef.MinecartCommandBlock.LAST_OUTPUT);
    }

    public void setLastOutput(@NotNull Component value) {
        metadata.set(MetadataDef.MinecartCommandBlock.LAST_OUTPUT, value);
    }

    @Override
    public int getObjectData() {
        return 6;
    }
}
