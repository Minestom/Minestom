package net.minestom.server;

public final class Git {
    private static final String COMMIT = "&COMMIT";
    private static final String BRANCH = "&BRANCH";

    private static final String GROUP = "&GROUP";
    private static final String ARTIFACT = "&ARTIFACT";


    public String commit() { return COMMIT; }
    public String branch() { return BRANCH; }

    public String group() { return GROUP; }
    public String artifact() { return ARTIFACT; }
}
