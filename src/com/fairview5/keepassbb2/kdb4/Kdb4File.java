/*
KeePass for BlackBerry
Copyright 2007 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

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
package com.fairview5.keepassbb2.kdb4;

import java.io.*;

import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.crypto.*;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.io.NoCopyByteArrayOutputStream;
import net.rim.device.api.system.*;
import net.rim.device.api.util.Arrays;

import com.fairview5.keepassbb2.common.crypto.*;
import com.fairview5.keepassbb2.common.file.KFile;
import com.fairview5.keepassbb2.common.io.IOUtils;
import com.fairview5.keepassbb2.common.ui.ProgressDialog;
import com.fairview5.keepassbb2.common.util.CommonUtils;
import com.fairview5.keepassbb2.common.xml.XMLNanoParser;

public class Kdb4File {

	public static final int PWM_DBSIG_1 = 0x9AA2D903;
	public static final int PWM_DBSIG_2_1XX = 0xB54BFB65;
	public static final int PWM_DBSIG_2_207 = 0xB54BFB66;
	public static final int PWM_DBSIG_2_208 = 0xB54BFB67;
	public static final int PWM_FILE_VERS_207 = 0x00010001;
	public static final int PWM_FILE_VERS_208 = 0x00010002;
	public static final int PWM_FILE_VERS_209 = 0x00020000;
	public static final int PWM_FILE_VERS_211 = 0x00020004;
	public static final int PWM_FILE_VERS_215 = 0x00030000;
	
	public static final int HEADER_EOH = 0;
	public static final int HEADER_COMMENT = 1;
	public static final int HEADER_CIPHERID = 2;
	public static final int HEADER_COMPRESSION = 3;
	public static final int HEADER_MASTERSEED = 4;
	public static final int HEADER_TRANSFORMSEED = 5;
	public static final int HEADER_TRANSFORMROUNDS = 6;
	public static final int HEADER_ENCRYPTIONIV = 7;
	public static final int HEADER_PROTECTEDKEY = 8;
	public static final int HEADER_STARTBYTES = 9;
	public static final int HEADER_RANDOMSTREAMID = 10;
	public static final int COMPRESSION_NONE = 0;
	public static final int COMPRESSION_GZIP = 1;
	public static final byte[] CRYPTO_AES_UUID = { (byte) 0x31, (byte) 0xC1,
			(byte) 0xF2, (byte) 0xE6, (byte) 0xBF, (byte) 0x71, (byte) 0x43,
			(byte) 0x50, (byte) 0xBE, (byte) 0x58, (byte) 0x05, (byte) 0x21,
			(byte) 0x6A, (byte) 0xFC, (byte) 0x5A, (byte) 0xFF };

	byte[] masterSeed = null;
	byte[] transformSeed = null;
	byte[] encryptionIV = null;
	byte[] protectedStreamKey = null;
	byte[] streamStartBytes = null;
	byte[] cipherUUID = null;
	public long keyEncryptionRounds;
	int compressionAlgorithm;
	int randomStreamID;

	byte[] transformedKey = null;
	public String fileName;
	public Kdb4Database kdb4;
	public static Kdb4File current;
	public boolean isInternal;

	public void setTransformedKey(String password, Kdb4KeyFile keyFile,
			int keyEncryptionRounds) {
		byte[] pk = null;
		this.keyEncryptionRounds = keyEncryptionRounds;
		if (password != null)
			pk = createPasswordKey(password);
		
		
		byte[] ck = createCompositeKey(pk, keyFile == null ? null : keyFile
				.getKey());
		transformedKey = createTransformedKey(ck, transformSeed,
				(int) this.keyEncryptionRounds);
		
		CommonUtils.logger("EncryptionRounds "+this.keyEncryptionRounds);
		CommonUtils.logger("CompositeKey:   "+CommonUtils.printBA(ck));
		CommonUtils.logger("TransformSeed:  "+CommonUtils.printBA(transformSeed));
		CommonUtils.logger("TransformedKey: "+CommonUtils.printBA(transformedKey));
		
	}

	public Kdb4Database newDatabase(KFile kf, String password,
			Kdb4KeyFile keyFile, int keyEncryptionRounds) throws IOException,
			CryptoException {
		kdb4 = new Kdb4Database();
		compressionAlgorithm = COMPRESSION_GZIP;
		transformSeed = new byte[32];
		RandomSource.getBytes(transformSeed, 0, transformSeed.length);
		setTransformedKey(password, keyFile, keyEncryptionRounds);
		fileName = kf.getFullName();
		save(kf);
		return kdb4;
	}

	public Kdb4Database newDatabase(String password, Kdb4KeyFile keyFile,
			int keyEncryptionRounds) throws IOException, CryptoException {
		kdb4 = new Kdb4Database();
		compressionAlgorithm = COMPRESSION_GZIP;
		transformSeed = new byte[32];
		RandomSource.getBytes(transformSeed, 0, transformSeed.length);
		setTransformedKey(password, keyFile, keyEncryptionRounds);
		fileName = "/internal";
		isInternal = true;
		save();
		return kdb4;
	}

	public Kdb4Database open(KFile kf, String password, Kdb4KeyFile keyFile)
			throws IOException, CryptoException {
		fileName = kf.getFullName();
		DataInputStream dis = kf.openDataInputStream();
		try {
			Kdb4Database kdb = open(dis, password, keyFile);
			return kdb;
		} finally {
			IOUtils.closeStream(dis);
		}
	}

	public Kdb4Database open(byte[] ba, String password, Kdb4KeyFile keyFile)
			throws IOException, CryptoException {
		ByteArrayInputStream bais = new ByteArrayInputStream(ba);
		try {
			Kdb4Database kdb = open(bais, password, keyFile);
			return kdb;
		} finally {
			IOUtils.closeStream(bais);
		}
	}

	public Kdb4Database open(InputStream is, String password, Kdb4KeyFile keyFile)
			throws IOException, CryptoException {
		DataInputStream dis = new DataInputStream(is);
		try {
			Kdb4Database kdb = open(dis, password, keyFile);
			return kdb;
		} finally {
			IOUtils.closeStream(dis);
		}
	}

	public Kdb4Database open(DataInputStream dis, String password,
			Kdb4KeyFile keyFile) throws IOException, CryptoException {
		InputStream cis = null;
		InputStream his = null;
		InputStream gis = null;
		try {

			readHeaders(dis);

			setTransformedKey(password, keyFile, (int) keyEncryptionRounds);

			byte[] fk = createFinalKey(masterSeed, transformedKey);

			String ks = "AES_256";
			String es = "AES/CBC/PKCS5";

			InitializationVector iv = new InitializationVector(encryptionIV);
			SymmetricKey sk = SymmetricKeyFactory
					.getInstance(ks, fk, 0, fk.length);
			cis = DecryptorFactory.getDecryptorInputStream(sk, dis, es, iv);

			byte[] storedBytes = new byte[32];
			IOUtils.readFully(cis, storedBytes);
			if (!Arrays.equals(storedBytes, this.streamStartBytes)) {
				CommonUtils.logger(CommonUtils.printBA("StartBytes ",
						streamStartBytes));
				CommonUtils
						.logger(CommonUtils.printBA("StoredBytes ", storedBytes));
				throw new CryptoIOException(
						"The file could not be decrypted using the supplied credentials.");
			}

			his = new HashedInputStream(cis);
			if (compressionAlgorithm == COMPRESSION_GZIP) {
				gis = new GZIPInputStream(his, 4096);
			} else {
				gis = his;
			}

			Kdb4NanoHandler h = new Kdb4NanoHandler(randomStreamID,
					protectedStreamKey);
			XMLNanoParser kp = new XMLNanoParser();
			kp.parse(gis, h);

			kdb4 = h.getDatabase();

			return kdb4;

		} finally {
			IOUtils.closeStream(gis);
			IOUtils.closeStream(his);
			IOUtils.closeStream(cis);
		}
	}

	public boolean isDirty() {
		if (kdb4 == null)
			return false;
		return kdb4.isDirty();
	}

	public boolean isValid() {
		return kdb4 != null;
	}

	public void close() {
		kdb4 = null;
	}

	public void save() throws IOException, CryptoException {
		if (isInternal) {
			saveInternal();
			return;
		}
		KFile kfo = new KFile(fileName);
		save(kfo);
	}

	public void save(KFile kfo) throws IOException, CryptoException {
		if (kfo.exists())
			kfo.truncate();
		else
			kfo.create();
		OutputStream os = kfo.openOutputStream();
		save(os);
		IOUtils.closeStream(kfo);
	}

	public void save(OutputStream os) throws IOException, CryptoException {
		OutputStream cos = null;
		OutputStream hos = null;
		OutputStream gos = null;
		OutputStreamWriter osw = null;

		try {

			masterSeed = new byte[32];
			encryptionIV = new byte[16];
			streamStartBytes = new byte[32];
			protectedStreamKey = new byte[32];
			compressionAlgorithm = COMPRESSION_GZIP;
			if (cipherUUID == null)
				cipherUUID = CRYPTO_AES_UUID;

			RandomSource.getBytes(masterSeed, 0, masterSeed.length);
			RandomSource.getBytes(encryptionIV, 0, encryptionIV.length);
			RandomSource.getBytes(streamStartBytes, 0, streamStartBytes.length);
			RandomSource
					.getBytes(protectedStreamKey, 0, protectedStreamKey.length);

			byte[] fk = createFinalKey(masterSeed, transformedKey);

			writeHeader(os);

			String ks = "AES_256";
			String es = "AES/CBC/PKCS5";

			InitializationVector iv = new InitializationVector(encryptionIV);
			SymmetricKey sk = SymmetricKeyFactory
					.getInstance(ks, fk, 0, fk.length);
			cos = EncryptorFactory.getEncryptorOutputStream(sk, os, es, iv);

			cos.write(streamStartBytes);
			cos.flush();

			hos = new HashedOutputStream(cos, 32768);
			if (compressionAlgorithm == COMPRESSION_GZIP) {
				gos = new GZIPOutputStream(hos, GZIPOutputStream.COMPRESSION_BEST,
						GZIPOutputStream.MAX_LOG2_WINDOW_LENGTH);
			} else {
				gos = hos;
			}
			ProgressDialog.setLegend("Encrypting and serializing...");
			kdb4.serialize(gos, 0, new Salsa20RandomStream(protectedStreamKey));

		} finally {
			IOUtils.closeWriter(osw);
			IOUtils.closeStream(gos);
			IOUtils.closeStream(hos);
			IOUtils.closeStream(cos);
			IOUtils.closeStream(os);
		}
	}

	public void saveAs(String filename) throws IOException, CryptoException {
		KFile kfo = new KFile(fileName);
		save(kfo);
	}

	public void saveAs(KFile kf) throws IOException, CryptoException {
		save(kf);
	}

	public void exportToXml(String filename) throws IOException, CryptoException {
		KFile kfo = null;
		OutputStream os = null;
		try {
			kfo = new KFile(filename);
			if (kfo.exists())
				kfo.truncate();
			else
				kfo.create();
			os = kfo.openOutputStream();

			kdb4.serialize(os, 0, null);
		} finally {
			IOUtils.closeStream(os);
			IOUtils.closeStream(kfo);
		}
	}

	void writeHeader(OutputStream out) throws IOException {
		IOUtils.writeSwappedInt(out, PWM_DBSIG_1);
		IOUtils.writeSwappedInt(out, PWM_DBSIG_2_208);
		IOUtils.writeSwappedInt(out, PWM_FILE_VERS_215);
		writeHeaderField(out, HEADER_CIPHERID, cipherUUID);
		writeHeaderField(out, HEADER_COMPRESSION, COMPRESSION_GZIP);
		writeHeaderField(out, HEADER_MASTERSEED, masterSeed);
		writeHeaderField(out, HEADER_TRANSFORMSEED, transformSeed);
		writeHeaderField(out, HEADER_TRANSFORMROUNDS, keyEncryptionRounds);
		writeHeaderField(out, HEADER_ENCRYPTIONIV, encryptionIV);
		writeHeaderField(out, HEADER_STARTBYTES, streamStartBytes);
		writeHeaderField(out, HEADER_PROTECTEDKEY, protectedStreamKey);
		writeHeaderField(out, HEADER_RANDOMSTREAMID,
				RandomStream.CRYPTO_RANDOM_SALSA20);
		writeEOHField(out);
		out.flush();
	}

	void writeHeaderField(OutputStream out, int fieldID, byte[] ba)
			throws IOException {
		out.write((byte) fieldID);
		IOUtils.writeSwappedShort(out, (short) ba.length);
		out.write(ba);
	}

	void writeHeaderField(OutputStream out, int fieldID, int i)
			throws IOException {
		out.write((byte) fieldID);
		IOUtils.writeSwappedShort(out, (short) 4);
		IOUtils.writeSwappedInt(out, i);
	}

	void writeHeaderField(OutputStream out, int fieldID, long l)
			throws IOException {
		out.write((byte) fieldID);
		IOUtils.writeSwappedShort(out, (short) 8);
		IOUtils.writeSwappedLong(out, l);
	}

	void writeHeaderField(OutputStream out, int fieldID, short s)
			throws IOException {
		out.write((byte) fieldID);
		IOUtils.writeSwappedShort(out, (short) 2);
		IOUtils.writeSwappedShort(out, s);
	}

	void writeEmptyHeaderField(OutputStream out, int fieldID) throws IOException {
		out.write((byte) fieldID);
		IOUtils.writeSwappedShort(out, (short) 0);
	}

	void writeEOHField(OutputStream out) throws IOException {
		out.write((byte) HEADER_EOH);
		IOUtils.writeSwappedShort(out, (short) 4);
		out.write(13);
		out.write(10);
		out.write(13);
		out.write(10);
	}

	byte[] createCompositeKey(byte[] key1, byte[] key2) {
		SHA256Digest hash = new SHA256Digest();
		if (key1 != null)
			hash.update(key1);
		if (key2 != null)
			hash.update(key2);
		return hash.getDigest();
	}

	byte[] createFinalKey(byte[] masterSeed, byte[] transformedKey) {
		SHA256Digest hash = new SHA256Digest();
		hash.update(masterSeed);
		hash.update(transformedKey);
		return hash.getDigest();
	}

	byte[] createPasswordKey(String pw) {
		SHA256Digest hash = new SHA256Digest();
		hash.update(pw.getBytes());
		return hash.getDigest();
	}

	public byte[] createTransformedKey(byte[] masterKey, byte[] masterSeed2,
			int numKeyEncRounds) {

		try {
			int i;

			AESKey ak = new AESKey(masterSeed2, 0, 256);
			AESEncryptorEngine aes = new AESEncryptorEngine(ak, 16);

			byte[][] temp = new byte[2][32];
			byte[] in = masterKey;
			byte[] out = temp[1];
			ProgressDialog.setLegend("Preparing key, " + numKeyEncRounds
					+ " rounds");
			for (i = 0; i < numKeyEncRounds; i++) {
				aes.encrypt(in, 0, out, 0);
				aes.encrypt(in, 16, out, 16);
				in = temp[(i + 1) % 2];
				out = temp[i % 2];
				if (numKeyEncRounds > 10 && i % (numKeyEncRounds / 10) == 0) {
					ProgressDialog.setProgress("Rounds Completed: " + i);
				}
			}
			ProgressDialog.setProgress("Rounds Completed: " + numKeyEncRounds);
			SHA256Digest md = new SHA256Digest();
			md.update(in);
			return md.getDigest();
		} catch (Throwable e) {
			throw new RuntimeException(e.toString());
		}

	}

	void readHeaders(InputStream in) throws IOException {
		int sig1 = IOUtils.readSwappedInt(in);
		if (sig1 != PWM_DBSIG_1) {
			throw new IOException("The file is not a valid KeePass database.");
		}
		int sig2 = IOUtils.readSwappedInt(in);
		if (sig2 != PWM_DBSIG_2_208) {
			throw new IOException("The database signature isn't 208");
		}
		int fver = IOUtils.readSwappedInt(in);
		if (!(fver == PWM_FILE_VERS_215 )) {
			throw new IOException(
					"The database must be from KeePass version 2.15");
		}
		boolean eof = false;
		while (!eof) {
			byte fid = (byte) in.read();
			short fsize = IOUtils.readSwappedShort(in);
			switch (fid) {
			case HEADER_EOH:
				in.read();
				in.read();
				in.read();
				in.read();
				eof = true;
				break;
			case HEADER_CIPHERID:
				cipherUUID = new byte[fsize];
				IOUtils.readFully(in, cipherUUID);
				break;
			case HEADER_MASTERSEED:
				masterSeed = new byte[fsize];
				IOUtils.readFully(in, masterSeed);
				break;
			case HEADER_TRANSFORMSEED:
				transformSeed = new byte[fsize];
				IOUtils.readFully(in, transformSeed);
				break;
			case HEADER_ENCRYPTIONIV:
				encryptionIV = new byte[fsize];
				IOUtils.readFully(in, encryptionIV);
				break;
			case HEADER_PROTECTEDKEY:
				protectedStreamKey = new byte[fsize];
				IOUtils.readFully(in, protectedStreamKey);
				break;
			case HEADER_STARTBYTES:
				streamStartBytes = new byte[fsize];
				IOUtils.readFully(in, streamStartBytes);
				break;
			case HEADER_TRANSFORMROUNDS:
				keyEncryptionRounds = IOUtils.readSwappedLong(in);
				break;
			case HEADER_COMPRESSION:
				compressionAlgorithm = IOUtils.readSwappedInt(in);
				break;
			case HEADER_RANDOMSTREAMID:
				randomStreamID = IOUtils.readSwappedInt(in);
				break;
			}
		}

		if (cipherUUID == null)
			throw new IOException(
					"The CipherUUID header was not found in the header.");
		if (masterSeed == null)
			throw new IOException(
					"The MasterSeed header was not found in the header.");
		if (transformSeed == null)
			throw new IOException(
					"The TransformSeed header was not found in the header.");
		if (encryptionIV == null)
			throw new IOException(
					"The EncryptionIV header was not found in the header.");
		if (streamStartBytes == null)
			throw new IOException(
					"The StartBytes header was not found in the header.");

	}

	public void saveInternal() throws IOException, CryptoException {
		PersistentObject po = PersistentStore
				.getPersistentObject(Kdb4PO.persistentStoreKey);
		NoCopyByteArrayOutputStream baos = new NoCopyByteArrayOutputStream();
		try {
			this.cipherUUID = CRYPTO_AES_UUID;
			save(baos);
			Kdb4PO kpo = new Kdb4PO();
			kpo.size = baos.size();
			kpo.ba = baos.getByteArray();
			int mh = CodeModuleManager.getModuleHandle("keepassbb2");
			CodeSigningKey csk = CodeSigningKey.get(mh, "F5EN");
			if (csk != null) {
				CommonUtils.logger("Signing db with key: " + csk.getDescription());
				po.setContents(new ControlledAccess(kpo, csk));
			} else {
				po.setContents(kpo);
			}
			po.forceCommit();
		} finally {
			IOUtils.closeStream(baos);
		}
	}

	public Kdb4Database openInternal(String password, Kdb4KeyFile kf)
			throws IOException, CryptoException {
		PersistentObject po = PersistentStore
				.getPersistentObject(Kdb4PO.persistentStoreKey);
		if (po == null)
			throw new FileNotFoundException("There is no internal database.");

		// int mh = CodeModuleManager.getModuleHandle("keepassbb2");
		// CodeSigningKey csk = CodeSigningKey.get(mh, "F5EN");
		Kdb4PO kpo = (Kdb4PO) po.getContents();

		if (kpo == null)
			throw new FileNotFoundException("There is no internal database.");
		ByteArrayInputStream bais = new ByteArrayInputStream(kpo.ba, 0, kpo.size);
		try {
			Kdb4Database k = open(bais, password, kf);
			isInternal = true;
			fileName = "/internal";
			return k;
		} finally {
			IOUtils.closeStream(bais);

		}
	}

	public static boolean checkForInternalDatabase() {
		PersistentObject po = PersistentStore
				.getPersistentObject(Kdb4PO.persistentStoreKey);
		if (po == null)
			return false;
		// int mh = CodeModuleManager.getModuleHandle("keepassbb2");
		// CodeSigningKey csk = CodeSigningKey.get(mh, "F5EN");
		Kdb4PO kpo = (Kdb4PO) po.getContents();
		if (kpo == null)
			return false;
		else
			return true;
	}
}
