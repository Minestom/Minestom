package net.minestom.server.entity;

public class RelativeFlags {
    public static final int NONE = 0x00;

    public static final int X = 0x01;
    public static final int Y = 0x02;
    public static final int Z = 0x04;

    public static final int YAW = 0x08;
    public static final int PITCH = 0x10;

    public static final int DELTA_X = 0x20;
    public static final int DELTA_Y = 0x40;
    public static final int DELTA_Z = 0x80;
    public static final int ROTATE_DELTA = 0x100;

    public static final int COORD = X | Y | Z;
    public static final int VIEW = YAW | PITCH;
    public static final int DELTA = DELTA_X | DELTA_Y | DELTA_Z | ROTATE_DELTA;
    public static final int ALL = COORD | VIEW | DELTA;
}