package net.minestom.server.extras.mojangAuth;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;

public class Encrypter extends MessageToByteEncoder<ByteBuf> {
    private final CipherBase cipher;

    public Encrypter(Cipher cipher) {
        this.cipher = new CipherBase(cipher);
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBufIn, ByteBuf byteBufOut) throws Exception {
        this.cipher.encrypt(byteBufIn, byteBufOut);
    }
}
