package com.fairview5.keepassbb2.common.crypto;

import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.crypto.SHA256Digest;
import net.rim.device.api.util.Arrays;

import com.fairview5.keepassbb2.common.io.IOUtils;

public class HashedInputStream extends InputStream {
	private InputStream in;
	private int blockIndex;
	private byte[] buffer = new byte[0];
	private int bufferPos;
	private SHA256Digest hash = new SHA256Digest();
	private boolean EOF;

	public HashedInputStream(InputStream in) {
		if (in == null)
			throw new RuntimeException("InputStream is null");
		this.in = in;
		EOF = false;
		blockIndex = 0;
		bufferPos = 0;
	}

	public int available() throws IOException {
		if (EOF)
			return 0;
		if (buffer.length - bufferPos > 0)
			return buffer.length - bufferPos;
		if (bufferPos == buffer.length) {
			int bl = readBlock();
			if (bl < 0)
				return 0;
			return bl;
		}
		return 0;
	}

	public int read(byte[] ba, int start, int len) throws IOException {
		if (EOF)
			return -1;
		int br = 0;
		while (br < len) {
			int remaining = len - br;
			int nextstart = start + br;
			int tocopy = Math.min(buffer.length - bufferPos, remaining);
			System.arraycopy(buffer, bufferPos, ba, nextstart, tocopy);
			bufferPos += tocopy;
			br += tocopy;
			if (bufferPos == buffer.length) {
				int bl = readBlock();
				if (bl < 0) {
					if (br == 0)
						return -1;
					break;
				}
			}
		}
		return br;
	}

	public int read() throws IOException {
		if (EOF)
			return -1;
		if (bufferPos == buffer.length) {
			int bs = readBlock();
			if (bs < 0)
				return -1;
		}
		return ((buffer[bufferPos++] & 0xff));
	}

	public int readBlock() throws IOException {
		int bi = IOUtils.readSwappedInt(in);
		if (bi != blockIndex)
			throw new HashMismatchException("Invalid block index");
		blockIndex++;
		byte[] storedHash = new byte[32];
		IOUtils.readFully(in, storedHash);
		int bl = IOUtils.readSwappedInt(in);
		if (bl == 0) {
			buffer = new byte[0];
			EOF = true;
			return -1;
		}
		if (buffer.length != bl) {
			buffer = new byte[bl];
			// Arrays.fill(buffer, (byte) 0);
		}
		IOUtils.readFully(in, buffer);
		hash.update(buffer);
		if (!Arrays.equals(storedHash, hash.getDigest())) {
			throw new HashMismatchException();
		}
		bufferPos = 0;
		return buffer.length;
	}

	public void close() throws IOException {
		in.close();
	}
}
