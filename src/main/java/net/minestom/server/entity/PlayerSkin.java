package net.minestom.server.entity;

/**
 * Contains all the data required to store a skin
 */
public class PlayerSkin {

    private String textures;
    private String signature;

    public PlayerSkin(String textures, String signature) {
        this.textures = textures;
        this.signature = signature;
    }

    /**
     * Get the skin textures value
     *
     * @return the textures value
     */
    public String getTextures() {
        return textures;
    }

    /**
     * Get the skin signature
     *
     * @return the skin signature
     */
    public String getSignature() {
        return signature;
    }
}
