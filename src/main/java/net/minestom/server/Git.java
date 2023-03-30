package net.minestom.server;

public final class Git {
    private static final String COMMIT = "&COMMIT";
    private static final String BRANCH = "&BRANCH";

    private static final String GROUP = "&GROUP";
    private static final String ARTIFACT = "&ARTIFACT";


    public static String commit() { return COMMIT; }
    public static String branch() { return BRANCH; }

    public static String group() { return GROUP; }
    public static String artifact() { return ARTIFACT; }
}
