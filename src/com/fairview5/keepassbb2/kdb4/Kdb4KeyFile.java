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

import java.io.IOException;

import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.crypto.SHA256Digest;
import net.rim.device.api.io.Base64InputStream;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.system.*;

import com.fairview5.keepassbb2.common.file.KFile;
import com.fairview5.keepassbb2.common.util.CommonUtils;

public class Kdb4KeyFile {

	private byte[] key = new byte[32];

	public Kdb4KeyFile(String filename) throws IOException {
		this(new KFile(filename));
	}

	public Kdb4KeyFile(KFile kf) throws IOException {
		byte[] ba = kf.readContents();
		importKeyText(ba);
		kf.close();
	}

	public Kdb4KeyFile() {
	}

	public Kdb4KeyFile(byte[] k) {
		importKeyText(k);
	}

	public void generateNew() {
		SHA256Digest d = new SHA256Digest();
		d.update(RandomSource.getBytes(1024));
		d.getDigest(key, 0);
	}

	public String exportKeyText() {
		return CommonUtils.printBA(getKey());
	}

	public void importKeyText(byte[] ba) {
		if (ba.length == 32) {
			System.arraycopy(ba, 0, key, 0, 32);
			return;
		}
		String fcs = new String(ba).trim();
		if (ba.length == 64 && CommonUtils.isHexString(fcs)) {
			key = new byte[32];
			for (int i = 0, j = 0; i < 32; i++, j += 2) {
				key[i] = (byte) (Integer.parseInt(fcs.substring(j, j + 2), 16) & 0xff);
			}
			return;
		} else if (fcs.indexOf("<Version>1.00</Version>") > 0
				&& fcs.indexOf("<Data>") > 0) {
			int ix1 = fcs.indexOf("<Data>") + 6;
			int ix2 = fcs.indexOf("</Data>");
			String bcs = null;
			try {
				bcs = fcs.substring(ix1, ix2);
				byte[] bb = Base64InputStream.decode(bcs);
				setKey(bb);
				return;
			} catch (Throwable e) {
				throw new RuntimeException(e.toString() + ":" + bcs);
			}

		}
		SHA256Digest md = new SHA256Digest();
		md.update(ba);
		key = md.getDigest();
	}

	public String toString() {
		return getKey() == null ? "<null>" : CommonUtils.printBA(getKey());
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] getKey() {
		return key;
	}

	public void saveInternal() throws IOException {
		PersistentObject po = PersistentStore
				.getPersistentObject(Kdb4KFPO.persistentStoreKey);
		Kdb4KFPO kpo = new Kdb4KFPO();
		System.arraycopy(key, 0, kpo.key, 0, key.length);

		int mh = CodeModuleManager.getModuleHandleForClass(this.getClass());
		CodeSigningKey csk = CodeSigningKey.get(mh, "F5EN");

		if (csk != null) {
			CommonUtils
					.logger("Signing keyfile with key: " + csk.getDescription());
			po.setContents(new ControlledAccess(kpo, csk));
		} else {
			po.setContents(kpo);
		}

		po.forceCommit();
	}

	public void loadInternal() throws IOException {
		PersistentObject po = PersistentStore
				.getPersistentObject(Kdb4KFPO.persistentStoreKey);
		if (po == null)
			throw new FileNotFoundException("There is no internal keyfile.");
		Kdb4KFPO kpo = (Kdb4KFPO) po.getContents();
		if (kpo == null)
			throw new FileNotFoundException("There is no internal keyfile.");
		System.arraycopy(kpo.key, 0, key, 0, key.length);
	}

	public static boolean checkForInternalKeyfile() {
		PersistentObject po = PersistentStore
				.getPersistentObject(Kdb4KFPO.persistentStoreKey);
		if (po == null)
			return false;
		Kdb4KFPO kpo = (Kdb4KFPO) po.getContents();
		if (kpo == null)
			return false;
		else
			return true;
	}

}
