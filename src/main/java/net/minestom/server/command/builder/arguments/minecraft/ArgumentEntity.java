package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Entity;

// TODO
public class ArgumentEntity extends Argument<Entity> {

    public ArgumentEntity(String id, boolean allowSpace) {
        super(id, allowSpace);
    }

    @Override
    public int getCorrectionResult(String value) {
        return 0;
    }

    @Override
    public Entity parse(String value) {
        return null;
    }

    @Override
    public int getConditionResult(Entity value) {
        return 0;
    }
}
