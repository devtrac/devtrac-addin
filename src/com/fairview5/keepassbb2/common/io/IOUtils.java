package com.fairview5.keepassbb2.common.io;

import java.io.*;

import com.fairview5.keepassbb2.common.file.KFile;

public class IOUtils {

	public static int readFully(InputStream in, byte[] ba) throws IOException {
		int br = 0;
		while (br < ba.length) {
			br += in.read(ba, br, ba.length - br);
		}
		return br;
	}

	public static int readInt(InputStream in) throws IOException {
		byte[] ba = new byte[4];
		readFully(in, ba);

		int i = ba[0] << 24 & 0xff000000 | ba[1] << 16 & 0x00ff0000 | ba[2] << 8
				& 0x0000ff00 | ba[3] << 0 & 0x000000ff;
		return i;
	}

	public static int readSwappedInt(InputStream in) throws IOException {
		byte[] ba = new byte[4];
		readFully(in, ba);

		int i = ba[3] << 24 & 0xff000000 | ba[2] << 16 & 0x00ff0000 | ba[1] << 8
				& 0x0000ff00 | ba[0] << 0 & 0x000000ff;
		return i;
	}

	public static void writeInt(OutputStream out, int i) throws IOException {
		byte[] ba = new byte[4];
		ba[0] = (byte) (i >> 24);
		ba[1] = (byte) (i >> 16);
		ba[2] = (byte) (i >>  8);
		ba[3] = (byte) (i >>  0);
		out.write(ba);
		return;
	}
	public static void writeSwappedInt(OutputStream out, int i) throws IOException {
		byte[] ba = new byte[4];
		ba[3] = (byte) (i >> 24);
		ba[2] = (byte) (i >> 16);
		ba[1] = (byte) (i >>  8);
		ba[0] = (byte) (i >>  0);
		out.write(ba);
		return;
	}

	public static short readShort(InputStream in) throws IOException {
		byte[] ba = new byte[2];
		readFully(in, ba);

		short i = (short) (ba[0] << 8 & 0x0000ff00 | ba[1] << 0 & 0x000000ff);
		return i;
	}

	public static short readSwappedShort(InputStream in) throws IOException {
		byte[] ba = new byte[2];
		readFully(in, ba);

		short i = (short) (ba[1] << 8 & 0x0000ff00 | ba[0] << 0 & 0x000000ff);
		return i;
	}
	public static void writeShort(OutputStream out, short i) throws IOException {
		byte[] ba = new byte[2];
		ba[0] = (byte) (i >>  8);
		ba[1] = (byte) (i >>  0);
		out.write(ba);
		return;
	}
	public static void writeSwappedShort(OutputStream out, short i) throws IOException {
		byte[] ba = new byte[2];
		ba[1] = (byte) (i >>  8);
		ba[0] = (byte) (i >>  0);
		out.write(ba);
		return;
	}


	public static long readLong(InputStream in) throws IOException {
		byte[] ba = new byte[8];
		readFully(in, ba);

		long l = ba[0] << 56 & 0xff00000000000000L | ba[1] << 48
				& 0x00ff000000000000L | ba[2] << 40 & 0x0000ff0000000000L
				| ba[3] << 32 & 0x000000ff00000000L | ba[4] << 24
				& 0x00000000ff000000L | ba[5] << 16 & 0x0000000000ff0000L
				| ba[6] << 8 & 0x000000000000ff00L | ba[7] << 0
				& 0x00000000000000ffL;
		return l;
	}

	public static long readSwappedLong(InputStream in) throws IOException {
		byte[] ba = new byte[8];
		readFully(in, ba);

		long l = ba[7] << 56 & 0xff00000000000000L | ba[6] << 48
				& 0x00ff000000000000L | ba[5] << 40 & 0x0000ff0000000000L
				| ba[4] << 32 & 0x000000ff00000000L | ba[3] << 24
				& 0x00000000ff000000L | ba[2] << 16 & 0x0000000000ff0000L
				| ba[1] << 8 & 0x000000000000ff00L | ba[0] << 0
				& 0x00000000000000ffL;
		return l;
	}
	
	public static void writeLong(OutputStream out, long i) throws IOException {
		byte[] ba = new byte[8];
		ba[0] = (byte) (i >> 56);
		ba[1] = (byte) (i >> 48);
		ba[2] = (byte) (i >> 40);
		ba[3] = (byte) (i >> 32);
		ba[4] = (byte) (i >> 24);
		ba[5] = (byte) (i >> 16);
		ba[6] = (byte) (i >>  8);
		ba[7] = (byte) (i >>  0);
		out.write(ba);
		return;
	}
	public static void writeSwappedLong(OutputStream out, long i) throws IOException {
		byte[] ba = new byte[8];
		ba[7] = (byte) (i >> 56);
		ba[6] = (byte) (i >> 48);
		ba[5] = (byte) (i >> 40);
		ba[4] = (byte) (i >> 32);
		ba[3] = (byte) (i >> 24);
		ba[2] = (byte) (i >> 16);
		ba[1] = (byte) (i >>  8);
		ba[0] = (byte) (i >>  0);
		out.write(ba);
		return;
	}

	public static void closeStream(InputStream in) {
		if (in == null)
			return;
		try {
			in.close();
		} catch (Exception e) {
		}
	}
	public static void closeReader(Reader in) {
		if (in == null)
			return;
		try {
			in.close();
		} catch (Exception e) {
		}
	}

	public static void closeStream(KFile f) {
		if (f == null)
			return;
		try {
			f.close();
		} catch (Throwable e) {
		}
	}

	public static void closeStream(OutputStream out) {
		if (out == null)
			return;
		try {
			out.flush();
		} catch (Exception e) {
		}
		try {
			out.close();
		} catch (Exception e) {
		}
	}

	public static void closeWriter(Writer out) {
		if (out == null)
			return;
		try {
			out.flush();
		} catch (Exception e) {
		}
		try {
			out.close();
		} catch (Exception e) {
		}
	}
	

}
