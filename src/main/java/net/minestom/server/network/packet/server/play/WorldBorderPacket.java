package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class WorldBorderPacket implements ServerPacket {

    public Action action;
    public WBAction wbAction;

    private static final WBAction DEFAULT_ACTION = new WBSetSize(0.0);

    /**
     * Default constructor, required for reflection operations.
     */
    public WorldBorderPacket() {
        action = Action.SET_SIZE;
        wbAction = DEFAULT_ACTION;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        wbAction.write(writer);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        action = Action.values()[reader.readVarInt()];
        wbAction = action.generateNewInstance();
        wbAction.read(reader);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WORLD_BORDER;
    }

    public enum Action {
        SET_SIZE(() -> new WBSetSize(0.0)),
        LERP_SIZE(() -> new WBLerpSize(0.0, 0.0, 0)),
        SET_CENTER(() -> new WBSetCenter(0.0, 0.0)),
        INITIALIZE(() -> new WBInitialize(0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0)),
        SET_WARNING_TIME(() -> new WBSetWarningTime(0)),
        SET_WARNING_BLOCKS(() -> new WBSetWarningBlocks(0));

        private Supplier<WBAction> generator;

        Action(Supplier<WBAction> generator) {
            this.generator = generator;
        }

        public WBAction generateNewInstance() {
            return generator.get();
        }
    }

    public static abstract class WBAction implements Writeable, Readable {}

    public static class WBSetSize extends WBAction {

        public double diameter;

        public WBSetSize(double diameter) {
            this.diameter = diameter;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeDouble(diameter);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            diameter = reader.readDouble();
        }
    }

    public static class WBLerpSize extends WBAction {

        public double oldDiameter;
        public double newDiameter;
        public long speed;

        public WBLerpSize(double oldDiameter, double newDiameter, long speed) {
            this.oldDiameter = oldDiameter;
            this.newDiameter = newDiameter;
            this.speed = speed;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeDouble(oldDiameter);
            writer.writeDouble(newDiameter);
            writer.writeVarLong(speed);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            oldDiameter = reader.readDouble();
            newDiameter = reader.readDouble();
            speed = reader.readVarLong();
        }
    }

    public static class WBSetCenter extends WBAction {

        public double x, z;

        public WBSetCenter(double x, double z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeDouble(x);
            writer.writeDouble(z);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            x = reader.readDouble();
            z = reader.readDouble();
        }
    }

    public static class WBInitialize extends WBAction {

        public double x, z;
        public double oldDiameter;
        public double newDiameter;
        public long speed;
        public int portalTeleportBoundary;
        public int warningTime;
        public int warningBlocks;

        public WBInitialize(double x, double z, double oldDiameter, double newDiameter, long speed,
                            int portalTeleportBoundary, int warningTime, int warningBlocks) {
            this.x = x;
            this.z = z;
            this.oldDiameter = oldDiameter;
            this.newDiameter = newDiameter;
            this.speed = speed;
            this.portalTeleportBoundary = portalTeleportBoundary;
            this.warningTime = warningTime;
            this.warningBlocks = warningBlocks;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeDouble(x);
            writer.writeDouble(z);
            writer.writeDouble(oldDiameter);
            writer.writeDouble(newDiameter);
            writer.writeVarLong(speed);
            writer.writeVarInt(portalTeleportBoundary);
            writer.writeVarInt(warningTime);
            writer.writeVarInt(warningBlocks);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            x = reader.readDouble();
            z = reader.readDouble();
            oldDiameter = reader.readDouble();
            newDiameter = reader.readDouble();
            speed = reader.readVarLong();
            portalTeleportBoundary = reader.readVarInt();
            warningTime = reader.readVarInt();
            warningBlocks = reader.readVarInt();
        }
    }

    public static class WBSetWarningTime extends WBAction {

        public int warningTime;

        public WBSetWarningTime(int warningTime) {
            this.warningTime = warningTime;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(warningTime);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            warningTime = reader.readVarInt();
        }
    }

    public static class WBSetWarningBlocks extends WBAction {

        public int warningBlocks;

        public WBSetWarningBlocks(int warningBlocks) {
            this.warningBlocks = warningBlocks;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(warningBlocks);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            warningBlocks = reader.readVarInt();
        }
    }

}
