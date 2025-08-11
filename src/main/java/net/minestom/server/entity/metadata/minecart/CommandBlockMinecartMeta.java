package net.minestom.server.entity.metadata.minecart;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class CommandBlockMinecartMeta extends AbstractMinecartMeta {
    public CommandBlockMinecartMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public String getCommand() {
        return metadata.get(MetadataDef.MinecartCommandBlock.COMMAND);
    }

    public void setCommand(String value) {
        metadata.set(MetadataDef.MinecartCommandBlock.COMMAND, value);
    }

    public Component getLastOutput() {
        return metadata.get(MetadataDef.MinecartCommandBlock.LAST_OUTPUT);
    }

    public void setLastOutput(Component value) {
        metadata.set(MetadataDef.MinecartCommandBlock.LAST_OUTPUT, value);
    }

    @Override
    public int getObjectData() {
        return 6;
    }
}
