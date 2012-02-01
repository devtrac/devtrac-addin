package com.fairview5.keepassbb2.common.crypto;

public interface RandomStream {

	public static final int CRYPTO_RANDOM_NONE = 0;
	public static final int CRYPTO_RANDOM_ARC4 = 1;
	public static final int CRYPTO_RANDOM_SALSA20 = 2;
	
	public void xorBytes(byte[] ba, int start, int len);
	public byte[] getBytes(int nByteCount);
	
}
