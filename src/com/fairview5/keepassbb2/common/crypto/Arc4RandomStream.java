package com.fairview5.keepassbb2.common.crypto;

import com.fairview5.keepassbb2.common.util.CommonUtils;

public class Arc4RandomStream implements RandomStream {
	private byte[] m_pbState = new byte[256];
	private int m_i = 0;
	private int m_j = 0;

	public Arc4RandomStream(byte[] pbKey) {
		CommonUtils.logger("Key: " + CommonUtils.printBA(pbKey));
		int uKeyLen = pbKey.length;
		int w, inxKey = 0;
		for (w = 0; w < 256; w++)
			m_pbState[w] = (byte) (w & 0xff);

		int i = 0, j = 0;
		byte t = 0;

		for (w = 0; w < 256; w++) // Key setup
		{
			j += ((m_pbState[w] + pbKey[inxKey]));
			j &= 0xff;

			t = m_pbState[i]; // Swap entries
			m_pbState[i] = m_pbState[j];
			m_pbState[j] = t;

			++inxKey;
			if (inxKey >= uKeyLen)
				inxKey = 0;
		}
		this.getBytes(512); // Throw away the first bytes
	}

	public byte[] getBytes(int uRequestedCount) {
		if (uRequestedCount == 0)
			return new byte[0];

		byte[] pbRet = new byte[uRequestedCount];
		int t;
		for (int w = 0; w < uRequestedCount; w++) {
			++m_i;
			m_i &= 0xff;
			m_j += m_pbState[m_i];
			m_j &= 0xff;

			t = m_pbState[m_i]; // Swap entries
			m_pbState[m_i] = m_pbState[m_j];
			m_pbState[m_j] = (byte) (t & 0xff);

			t = (byte) (m_pbState[m_i] + m_pbState[m_j]);
			pbRet[w] = m_pbState[t & 0xff];
		}
		return pbRet;
	}

	public void xorBytes(byte[] ba, int start, int len) {
		byte[] ra = getBytes(len);
		for (int i = 0; i < len; i++) {
			ba[start + i] ^= ra[i];
		}
	}
}
