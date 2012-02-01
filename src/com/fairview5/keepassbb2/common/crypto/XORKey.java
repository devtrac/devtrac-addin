package com.fairview5.keepassbb2.common.crypto;

import net.rim.device.api.crypto.*;

public class XORKey implements SymmetricKey {
	protected byte[] key;

	public XORKey(byte[] data, int offset, int length) throws InvalidKeyException  {
		MD5Digest m = new MD5Digest();
		m.update(data, offset, length/8);
		key = m.getDigest();
	}

	public int getBitLength() throws CryptoTokenException,
			CryptoUnsupportedOperationException {
		return 128;
	}

	public byte[] getData() throws CryptoTokenException,
			CryptoUnsupportedOperationException {
		return key;
	}

	public int getLength() throws CryptoTokenException,
			CryptoUnsupportedOperationException {
		return 16;
	}

	public SymmetricCryptoToken getSymmetricCryptoToken() {
		return null;
	}

	public String getAlgorithm() {
		return "XOR";
	}

	public static class Factory extends SymmetricKeyFactory {

		protected SymmetricKey create(String algorithm, byte[] data, int offset, int length)
				throws NoSuchAlgorithmException {
			try {
				return new XORKey(data, offset, length);
			} catch (InvalidKeyException e) {
				throw new NoSuchAlgorithmException(e.toString());
			}
		}

		protected int getDefaultKeyLength(String algorithm) throws NoSuchAlgorithmException {
			return 128;
		}

		protected String[] getFactoryAlgorithms() {
			return new String[] { "XOR" };
		}

	}
	
}
