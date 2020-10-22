package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class WorldBorderPacket implements ServerPacket {

    public Action action;
    public WBAction wbAction;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        wbAction.write(writer);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WORLD_BORDER;
    }

    public enum Action {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS
    }

    public static abstract class WBAction {
        public abstract void write(BinaryWriter writer);
    }

    public static class WBSetSize extends WBAction {

        public final double diameter;

        public WBSetSize(double diameter) {
            this.diameter = diameter;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeDouble(diameter);
        }
    }

    public static class WBLerpSize extends WBAction {

        public final double oldDiameter;
        public final double newDiameter;
        public final long speed;

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
    }

    public static class WBSetCenter extends WBAction {

        public final double x, z;

        public WBSetCenter(double x, double z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeDouble(x);
            writer.writeDouble(z);
        }
    }

    public static class WBInitialize extends WBAction {

        public final double x, z;
        public final double oldDiameter;
        public final double newDiameter;
        public final long speed;
        public final int portalTeleportBoundary;
        public final int warningTime;
        public final int warningBlocks;

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
    }

    public static class WBSetWarningTime extends WBAction {

        public final int warningTime;

        public WBSetWarningTime(int warningTime) {
            this.warningTime = warningTime;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(warningTime);
        }
    }

    public static class WBSetWarningBlocks extends WBAction {

        public final int warningBlocks;

        public WBSetWarningBlocks(int warningBlocks) {
            this.warningBlocks = warningBlocks;
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(warningBlocks);
        }
    }

}
