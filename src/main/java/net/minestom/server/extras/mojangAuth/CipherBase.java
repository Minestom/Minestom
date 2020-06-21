package net.minestom.server.extras.mojangAuth;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class CipherBase {
   private final Cipher cipher;
   private byte[] inTempArray = new byte[0];
   private byte[] outTempArray = new byte[0];

   protected CipherBase(Cipher cipher) {
      this.cipher = cipher;
   }

   private byte[] bufToByte(ByteBuf buffer) {
      int remainingBytes = buffer.readableBytes();

      // Need to resize temp array
      if (inTempArray.length < remainingBytes) {
         inTempArray = new byte[remainingBytes];
      }

      buffer.readBytes(inTempArray, 0, remainingBytes);
      return inTempArray;
   }

   protected ByteBuf decrypt(ChannelHandlerContext channelHandlerContext, ByteBuf byteBufIn) throws ShortBufferException {
      int remainingBytes = byteBufIn.readableBytes();
      byte[] bytes = bufToByte(byteBufIn);

      ByteBuf outputBuffer = channelHandlerContext.alloc().heapBuffer(cipher.getOutputSize(remainingBytes));
      outputBuffer.writerIndex(cipher.update(bytes, 0, remainingBytes, outputBuffer.array(), outputBuffer.arrayOffset()));

      return outputBuffer;
   }

   protected void encrypt(ByteBuf byteBufIn, ByteBuf byteBufOut) throws ShortBufferException {
      int remainingBytes = byteBufIn.readableBytes();
      byte[] bytes = bufToByte(byteBufIn);
      int newSize = cipher.getOutputSize(remainingBytes);

      // Need to resize temp array
      if (outTempArray.length < newSize) {
         outTempArray = new byte[newSize];
      }

      byteBufOut.writeBytes(outTempArray, 0, cipher.update(bytes, 0, remainingBytes, outTempArray));
   }
}
