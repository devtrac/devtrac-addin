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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.DataBuffer;

public class Kdb4SyncObject implements SyncObject {
	public static final int UID = 100;
	public static final byte FIELD_TYPE_DATA = 101;
	public static final byte FIELD_TYPE_CHECKSUM = 102;
	public String filename;
	int dbLength;
	int dbFieldType;
	byte[] db;

	public Kdb4SyncObject() {
		PersistentObject po = PersistentStore.getPersistentObject(Kdb4PO.persistentStoreKey);
		if (po == null) throw new RuntimeException("po is null");
		Kdb4PO kpo = (Kdb4PO) po.getContents();
		if (kpo == null) throw new RuntimeException("ba is null");
		db = kpo.ba;
		dbLength  = kpo.size;
		this.dbFieldType = FIELD_TYPE_DATA;
	}

	public Kdb4SyncObject(DataBuffer dataBuffer) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int chunk = 0;
		while(dataBuffer.available() > 0) {
			byte[] ba = ConverterUtilities.readByteArray(dataBuffer, true);
			baos.write(ba);
			chunk++;
		}
		this.db = baos.toByteArray();
		this.dbLength = baos.size();
		this.dbFieldType = FIELD_TYPE_DATA;
		baos.close();
		
	}

	public void save() throws IOException, CryptoException {
		PersistentObject po = PersistentStore.getPersistentObject(Kdb4PO.persistentStoreKey);
		Kdb4PO kpo = new Kdb4PO();
		kpo.size = dbLength;
		kpo.ba = db; 
		po.setContents(kpo);
		po.forceCommit();
	}

	public boolean convert(DataBuffer dataBuffer) {
		int len = dbLength;
		if (len == 0) {
			ConverterUtilities.writeEmptyField(dataBuffer,
					Kdb4SyncObject.FIELD_TYPE_DATA);
			return (true);
		}

		for (int i = 0; i < len;) {
			int csize = Math.min(8192, len - i);
			dataBuffer.writeShort(csize);
			dataBuffer.writeByte(dbFieldType);
			dataBuffer.write(db, i, csize);
			i += csize;
		}
		return true;
	}

	public int getUID() {
		return UID;
	}
}
