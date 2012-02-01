/*
KeePass for BlackBerry
Copyright 2007,2008 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fairview5.keepassbb2.common.crypto;

import java.io.IOException;
import java.io.OutputStream;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.EncryptorFactory;
import net.rim.device.api.crypto.InitializationVector;
import net.rim.device.api.crypto.Key;
import net.rim.device.api.crypto.NoSuchAlgorithmException;
import net.rim.device.api.crypto.SymmetricKeyEncryptorEngine;

public class TwofishEncryptorEngine implements SymmetricKeyEncryptorEngine {

	TwofishKey key;
	int	blockLength;
	int keyLength;
	
	public TwofishEncryptorEngine(TwofishKey k) throws CryptoUnsupportedOperationException, CryptoTokenException {
		this(k, 16);
	}
	
	public TwofishEncryptorEngine(TwofishKey key, int blockLength) throws CryptoUnsupportedOperationException, CryptoTokenException {
		if (blockLength != 16) throw new CryptoUnsupportedOperationException("Block length must be 16");
		this.key = key;
		this.keyLength = key.getLength();
		this.blockLength = blockLength;
	}
	
	public void encrypt(byte[] plaintext, int plaintextOffset, byte[] ciphertext, int ciphertextOffset)
			throws CryptoTokenException {
		
		TwofishImpl.blockEncrypt(plaintext, plaintextOffset, ciphertext, ciphertextOffset, key);
		
	}

	public String getAlgorithm() {
		return "Twofish_" + (keyLength * 8) + "_" + (blockLength * 8);
	}

	public int getBlockLength() {
		return blockLength;
	}
	
	public static class Factory extends EncryptorFactory {

		protected Object create(String algorithm, String nextAlgorithm, Key key, OutputStream stream,
				InitializationVector iv) throws NoSuchAlgorithmException, ClassCastException, CryptoTokenException,
				CryptoUnsupportedOperationException, CryptoException, IOException {
			
			return new TwofishEncryptorEngine((TwofishKey)key);
		}

		protected String[] getFactoryAlgorithms() {
			return new String[] {"Twofish"};
		}
		
	}

}
