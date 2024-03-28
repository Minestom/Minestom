package net.minestom.server.entity;

public class RelativeFlags {
    public static final int NONE = 0x00;
    public static final int X = 0x01;
    public static final int Y = 0x02;
    public static final int Z = 0x04;
    public static final int YAW = 0x08;
    public static final int PITCH = 0x10;
    public static final int COORD = X | Y | Z;
    public static final int VIEW = YAW | PITCH;
    public static final int ALL = COORD | VIEW;
}