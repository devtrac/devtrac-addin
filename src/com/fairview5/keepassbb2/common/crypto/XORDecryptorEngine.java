package com.fairview5.keepassbb2.common.crypto;

import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.crypto.*;

public class XORDecryptorEngine implements SymmetricKeyDecryptorEngine {

	XORKey key;
	int blockLength;
	int keyLength;

	public XORDecryptorEngine(XORKey k) throws CryptoUnsupportedOperationException, CryptoTokenException {
		this(k, 16);
	}

	public XORDecryptorEngine(XORKey key, int blockLength) throws CryptoUnsupportedOperationException,
			CryptoTokenException {
		if (blockLength != 16)
			throw new CryptoUnsupportedOperationException("Block length must be 16");
		this.key = key;
		this.keyLength = key.getLength();
		this.blockLength = blockLength;
	}
	
	
	public void decrypt(byte[] ciphertext, int ciphertextOffset,
			byte[] plaintext, int plaintextOffset) throws CryptoTokenException {
		for(int i=0;i<16;i++) {
			plaintext[plaintextOffset+i] = (ciphertext[ciphertextOffset+i]);// ^ key.key[i]);
		}
		
		
	}

	public String getAlgorithm() {
		return "XOR_" + (keyLength * 8) + "_" + (blockLength * 8);
	}

	public int getBlockLength() {
		return blockLength;
	}

	public static class Factory extends DecryptorFactory {

		protected Object create(String algorithm, String nextAlgorithm, Key key, InputStream stream,
				InitializationVector iv) throws NoSuchAlgorithmException, ClassCastException, CryptoTokenException,
				CryptoUnsupportedOperationException, CryptoException, IOException {

			return new XORDecryptorEngine((XORKey) key);

		}

		protected String[] getFactoryAlgorithms() {
			return new String[] { "XOR" };
		}

	}
}
