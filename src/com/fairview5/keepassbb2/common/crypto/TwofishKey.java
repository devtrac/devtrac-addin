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
//Cryptix General License
//
//Copyright (c) 1995-2005 The Cryptix Foundation Limited.
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions are
//met:
//
//  1. Redistributions of source code must retain the copyright notice,
//     this list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright
//     notice, this list of conditions and the following disclaimer in
//     the documentation and/or other materials provided with the
//     distribution.
//
//THIS SOFTWARE IS PROVIDED BY THE CRYPTIX FOUNDATION LIMITED AND
//CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
//INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//IN NO EVENT SHALL THE CRYPTIX FOUNDATION LIMITED OR CONTRIBUTORS BE
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
//BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
//OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
//IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.fairview5.keepassbb2.common.crypto;

import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.InvalidKeyException;
import net.rim.device.api.crypto.NoSuchAlgorithmException;
import net.rim.device.api.crypto.SymmetricCryptoToken;
import net.rim.device.api.crypto.SymmetricKey;
import net.rim.device.api.crypto.SymmetricKeyFactory;
import net.rim.device.api.util.Persistable;


public class TwofishKey implements SymmetricKey, Persistable {
	public int[] sBox;
	public int[] subKeys;
	int keyLength;

	public TwofishKey(byte[] k) throws InvalidKeyException {
		this(k, 0, k.length * 8);
	}

	public TwofishKey(byte[] k, int keyOffset, int bitLength) throws InvalidKeyException {
		keyLength = bitLength / 8;
		int[][] i = TwofishImpl.makeKey(k, keyOffset, bitLength);
		sBox = i[0];
		subKeys = i[1];
	}
	
	public int getBitLength() throws CryptoTokenException, CryptoUnsupportedOperationException {
		return keyLength * 8;
	}

	public byte[] getData() throws CryptoTokenException, CryptoUnsupportedOperationException {
		return null;
	}

	public int getLength() throws CryptoTokenException, CryptoUnsupportedOperationException {
		return keyLength;
	}

	public SymmetricCryptoToken getSymmetricCryptoToken() {
		return null;
	}

	public String getAlgorithm() {
		return "Twofish";
	}

	public static class Factory extends SymmetricKeyFactory {

		protected SymmetricKey create(String algorithm, byte[] data, int offset, int length)
				throws NoSuchAlgorithmException {
			try {
				return new TwofishKey(data, offset, length);
			} catch (InvalidKeyException e) {
				throw new NoSuchAlgorithmException(e.toString());
			}
		}

		protected int getDefaultKeyLength(String algorithm) throws NoSuchAlgorithmException {
			return 128;
		}

		protected String[] getFactoryAlgorithms() {
			return new String[] { "Twofish" };
		}

	}
}
