package com.fairview5.keepassbb2.common.crypto;

import java.io.IOException;
import java.io.OutputStream;

import com.fairview5.keepassbb2.common.io.IOUtils;

import net.rim.device.api.crypto.SHA256Digest;

public class HashedOutputStream extends OutputStream {
	private OutputStream oS;
	private int blockSize;
	private int blockIndex;
	private byte[] buffer;
	private int bufferPos;
	private SHA256Digest hash;
	
	public HashedOutputStream(OutputStream out) {
		this(out, 200);
	}
	public HashedOutputStream(OutputStream out, int blockSize){
		oS = out;
		this.blockSize = blockSize;
		buffer = new byte[blockSize];
		bufferPos = 0;
		blockIndex = 0;
		hash = new SHA256Digest();
	}
	public void write(int b) throws IOException {
		buffer[bufferPos++] = (byte)b;
		if (bufferPos == blockSize) {
			writeBlock();
		}
	}
	private void writeBlock() throws IOException{
		IOUtils.writeSwappedInt(oS, blockIndex++);
		if (bufferPos > 0) {
			hash.reset();
			hash.update(buffer, 0, bufferPos);
			oS.write(hash.getDigest());
		} else {
			for(int i=0;i<32;i++) oS.write(0);
		}
		IOUtils.writeSwappedInt(oS, bufferPos);
		if (bufferPos > 0) {
			oS.write(buffer, 0, bufferPos);
		}
		bufferPos = 0;
	}
	public void flush() throws IOException {
		oS.flush();
	}
	public void close() throws IOException {
		if (bufferPos == 0) {
			writeBlock();
		} else {
			writeBlock();
			writeBlock();
		}
		flush();
		oS.close();
	}
}
