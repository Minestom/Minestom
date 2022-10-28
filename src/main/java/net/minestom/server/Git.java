package net.minestom.server;

public final class Git {
    private static final String COMMIT = "&COMMIT";
    private static final String BRANCH = "&BRANCH";

    private static final String GROUP = "&GROUP";
    private static final String ARTIFACT = "&ARTIFACT";



    public String getCommit() { return COMMIT; }
    public String getBranch() { return BRANCH; }

    public String getGroup() { return GROUP; }
    public String getArtifact() { return ARTIFACT; }
}
