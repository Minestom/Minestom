package net.minestom.server.extras.mojangAuth;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import java.util.List;

public class Decrypter extends MessageToMessageDecoder<ByteBuf> {
    private final CipherBase cipher;

    public Decrypter(Cipher cipher) {
        this.cipher = new CipherBase(cipher);
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        list.add(this.cipher.decrypt(channelHandlerContext, byteBuf));
    }
}
