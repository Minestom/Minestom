package net.minestom.server.entity;

public class PlayerSkin {

    private String textures;
    private String signature;

    public PlayerSkin(String textures, String signature) {
        this.textures = textures;
        this.signature = signature;
    }

    public String getTextures() {
        return textures;
    }

    public void setTextures(String textures) {
        this.textures = textures;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
