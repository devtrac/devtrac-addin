/*
 * The MIT License

Copyright (c) 2009 Fairview 5 Engineering, LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

 * @author      Thomas Dixon

 */

package com.fairview5.keepassbb2.common.crypto;

import net.rim.device.api.crypto.SHA256Digest;

public class Salsa20RandomStream implements RandomStream {

   /** Internal state */
   private int[] state = new int[16];
   
   /** Constants */
   private final byte[]
       sigma = new String("expand 32-byte k").getBytes(),
       tau   = new String("expand 16-byte k").getBytes();
   
   /** Keystream and index marker */
   private int index = 0;
   private byte[] keyStream = new byte[64];
   
	public Salsa20RandomStream(byte[] pbKey)
	{
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(pbKey);
		byte[] pbKey32 = sha256.getDigest();
		byte[] pbIV = new byte[]{ (byte)0xE8, 0x30, 0x09, 0x4B,
			(byte)0x97, 0x20, 0x5D, 0x2A }; // Unique constant
		setKey(pbKey32);
		setIV(pbIV);
		index = 0;
	}
   
   public void setKey(byte[] key) {
           
           int offSet = 0;
           byte[] constants = null;
           
           state[1] = byteToIntLittle(key, 0);
           state[2] = byteToIntLittle(key, 4);
           state[3] = byteToIntLittle(key, 8);
           state[4] = byteToIntLittle(key, 12);
                       
           if (key.length == 32) {
               constants = sigma;
               offSet = 16;
           } else
               constants = tau;
                       
           state[11] = byteToIntLittle(key, offSet);
           state[12] = byteToIntLittle(key, offSet+4);
           state[13] = byteToIntLittle(key, offSet+8);
           state[14] = byteToIntLittle(key, offSet+12);
           state[0 ] = byteToIntLittle(constants, 0);
           state[5 ] = byteToIntLittle(constants, 4);
           state[10] = byteToIntLittle(constants, 8);
           state[15] = byteToIntLittle(constants, 12);
   }
   
   public void setIV(byte[] iv) {
           state[6] = byteToIntLittle(iv, 0);
           state[7] = byteToIntLittle(iv, 4);
           state[8] = state[9] = 0;
       }
   
   public void crypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
       for (int i = 0; i < len; i++) {
           if (index == 0) {
               keyStream = salsa20WordToByte(state);
               state[8]++;
               if (state[8] == 0) state[9]++;
               // As in djb's, changing the IV after 2^70 bytes is the user's responsibility
           }
           out[outOff+i] = (byte)(keyStream[index]^in[inOff+i]);
           index = (index + 1) & 0x3f;
       }      
   }
   
   public byte[] crypt(byte[] in) {
       byte[] out = new byte[in.length];
       crypt(in, 0, in.length, out, 0);
       return out;
   }
   
   private byte[] salsa20WordToByte(int[] input) {
       int[] x = new int[input.length];
       System.arraycopy(input, 0, x, 0, input.length);
       
       int i = 0;
       while (i++ < 10) {
           x[ 4] ^= rotl((x[ 0]+x[12]), 7);
           x[ 8] ^= rotl((x[ 4]+x[ 0]), 9);
           x[12] ^= rotl((x[ 8]+x[ 4]),13);
           x[ 0] ^= rotl((x[12]+x[ 8]),18);
           x[ 9] ^= rotl((x[ 5]+x[ 1]), 7);
           x[13] ^= rotl((x[ 9]+x[ 5]), 9);
           x[ 1] ^= rotl((x[13]+x[ 9]),13);
           x[ 5] ^= rotl((x[ 1]+x[13]),18);
           x[14] ^= rotl((x[10]+x[ 6]), 7);
           x[ 2] ^= rotl((x[14]+x[10]), 9);
           x[ 6] ^= rotl((x[ 2]+x[14]),13);
           x[10] ^= rotl((x[ 6]+x[ 2]),18);
           x[ 3] ^= rotl((x[15]+x[11]), 7);
           x[ 7] ^= rotl((x[ 3]+x[15]), 9);
           x[11] ^= rotl((x[ 7]+x[ 3]),13);
           x[15] ^= rotl((x[11]+x[ 7]),18);
           x[ 1] ^= rotl((x[ 0]+x[ 3]), 7);
           x[ 2] ^= rotl((x[ 1]+x[ 0]), 9);
           x[ 3] ^= rotl((x[ 2]+x[ 1]),13);
           x[ 0] ^= rotl((x[ 3]+x[ 2]),18);
           x[ 6] ^= rotl((x[ 5]+x[ 4]), 7);
           x[ 7] ^= rotl((x[ 6]+x[ 5]), 9);
           x[ 4] ^= rotl((x[ 7]+x[ 6]),13);
           x[ 5] ^= rotl((x[ 4]+x[ 7]),18);
           x[11] ^= rotl((x[10]+x[ 9]), 7);
           x[ 8] ^= rotl((x[11]+x[10]), 9);
           x[ 9] ^= rotl((x[ 8]+x[11]),13);
           x[10] ^= rotl((x[ 9]+x[ 8]),18);
           x[12] ^= rotl((x[15]+x[14]), 7);
           x[13] ^= rotl((x[12]+x[15]), 9);
           x[14] ^= rotl((x[13]+x[12]),13);
           x[15] ^= rotl((x[14]+x[13]),18);
       }
       
       for (i = 0; i < 16; i++)
           x[i] += input[i];
       
       return intToByteLittle(x);
   }
   
   private byte[] intToByteLittle(int x) {
       byte[] out = new byte[4];
       out[0] = (byte)x;
       out[1] = (byte)(x >>> 8);
       out[2] = (byte)(x >>> 16);
       out[3] = (byte)(x >>> 24);
       return out;
   }
   
   private byte[] intToByteLittle(int[] x) {
       byte[] out = new byte[4*x.length];
       for (int i = 0, j = 0; i < x.length; i++,j+=4)
           System.arraycopy(intToByteLittle(x[i]), 0, out, j, 4);
       return out;
   }
   
   private int rotl(int x, int y) {
       return (x << y) | (x >>> (32-y));
   }
   
   private int byteToIntLittle(byte[] x, int offset) {
       return ((x[offset++] & 255)      ) |
       ((x[offset++] & 255) <<  8) |
       ((x[offset++] & 255) << 16) |
       (x[offset++] << 24);
   }	
	public byte[] getBytes(int byteCount) {
		// TODO Auto-generated method stub
		return null;
	}

	public void xorBytes(byte[] ba, int start, int len) {
		
      for (int i = 0; i < len; i++) {
         if (index == 0) {
             keyStream = salsa20WordToByte(state);
             state[8]++;
             if (state[8] == 0) state[9]++;
             // As in djb's, changing the IV after 2^70 bytes is the user's responsibility
         }
         ba[start+i] ^= (keyStream[index]);
         index = (index + 1) & 0x3f;
     }      

	}

}
