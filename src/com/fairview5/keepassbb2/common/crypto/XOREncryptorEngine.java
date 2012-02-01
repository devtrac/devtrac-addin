package com.fairview5.keepassbb2.common.crypto;

import java.io.IOException;
import java.io.OutputStream;

import net.rim.device.api.crypto.*;

public class XOREncryptorEngine implements SymmetricKeyEncryptorEngine {
	XORKey key;
	int blockLength;
	int keyLength;
	
	public XOREncryptorEngine(XORKey k) throws CryptoUnsupportedOperationException, CryptoTokenException {
		this(k, 16);
	}

	public XOREncryptorEngine(XORKey key, int blockLength) throws CryptoTokenException, CryptoUnsupportedOperationException {
		this.key = key;
		this.keyLength = key.getLength();
		this.blockLength = blockLength;
	}

	public void encrypt(byte[] plaintext, int plaintextOffset,
			byte[] ciphertext, int ciphertextOffset) throws CryptoTokenException {

		for(int i=0;i<16;i++) {
			ciphertext[ciphertextOffset+i] = (plaintext[plaintextOffset+i]);// ^= key.key[i]);
		}

	}

	public String getAlgorithm() {
		return "XOR_" + (keyLength * 8) + "_" + (blockLength * 8);
	}

	public int getBlockLength() {
		return blockLength;
	}

	public static class Factory extends EncryptorFactory {

		protected Object create(String algorithm, String nextAlgorithm, Key key, OutputStream stream,
				InitializationVector iv) throws NoSuchAlgorithmException, ClassCastException, CryptoTokenException,
				CryptoUnsupportedOperationException, CryptoException, IOException {
			
			return new XOREncryptorEngine((XORKey)key);
		}

		protected String[] getFactoryAlgorithms() {
			return new String[] {"XOR"};
		}
		
	}
	
}
