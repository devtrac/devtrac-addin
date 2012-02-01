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

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.io.NoCopyByteArrayOutputStream;
import net.rim.device.api.synchronization.*;
import net.rim.device.api.system.*;
import net.rim.device.api.util.DataBuffer;

import com.fairview5.keepassbb2.common.util.CommonUtils;

public class Kdb4Synchronizer implements SyncCollection, SyncConverter {
	
	static {
	}

	private static final int RC_DEVICE_FILE_SERIALIZE_EXCEPTION = 12;
	//private static final int RC_NOT_SUPPORTED = 13;
	private static final int RC_NO_CHANGES_MADE = 1;
	private static final int RC_CHANGES_MADE = 2;
	private static final int RC_RECONCILE_EXCEPTION = 3;
	private static final int RC_DESKTOP_DECRYPT_FAILURE = 4;
	private static final int RC_DEVICE_DECRYPT_FAILURE = 5;
	private static final int RC_CONVERT_OUT_ERROR = 6;
	private static final int RC_CONVERT_IN_ERROR = 7;
	private static final int RC_RESTORE = 8;
	private static final int RC_BACKUP = 9;
	private static final int RC_LOADED_TO_DEVICE = 10;
	private static final int RC_DEVICE_FILE_OPEN_EXCEPTION = 11;

	public static final int FIELD_RECORD_TYPE = 0;

	public static final long GUID_BASE = CommonUtils.initializeGUID("com.fairview5.keepassbb2.util");

	public static final long GUID_SYNCSTART = CommonUtils
			.createGUID("GUID_SYNCSTART");
	public static final long GUID_SYNCPROCEED = CommonUtils
			.createGUID("GUID_SYNCPROCEED");
	public static final long GUID_SYNCCOMPLETE = CommonUtils
			.createGUID("GUID_SYNCCOMPLETE");
	public static final long RS_SYNC = CommonUtils.createGUID("RS_SYNC");

	
	private static StringBuffer sbProgress = null;
	private boolean inTransaction = false;
	String password;
	SyncMessage psMessage = new SyncMessage();
	SyncCredentials psCredentials;
	Kdb4SyncObject kdb4DX;

	public Kdb4Synchronizer() {
	}
	
	public void enableSync() {
		SyncManager.getInstance().enableSynchronization(this, true);
		saveMe();
	}
	public void disableSync() {
		SyncManager.getInstance().disableSynchronization(this);
		removeMe();
	}
	
	
	public void saveMe() {
		RuntimeStore.getRuntimeStore().put(RS_SYNC, this);
	}
	public void removeMe() {
		RuntimeStore.getRuntimeStore().remove(RS_SYNC);
	}
	public static Kdb4Synchronizer getMe() {
		return (Kdb4Synchronizer)RuntimeStore.getRuntimeStore().get(RS_SYNC);
	}
	
	private static void progress(String msg) {
		System.out.println("[keePass]" + msg);
		
		if (sbProgress != null)
			sbProgress.append(msg + '\n');
		else {
			CommonUtils.registerLogger("KeePassBB2");
			CommonUtils.logger(msg);
		}

	}

	private void progress(int rc, final String msg) {
		progress(msg);
		psMessage.message = msg;
		psMessage.rc = rc;
	}

	public boolean addSyncObject(SyncObject syncObject) {
		progress("AddSyncObject " + syncObject.getClass().getName());
		if (syncObject instanceof Kdb4SyncObject) {
			kdb4DX = (Kdb4SyncObject) syncObject;
			return true;
		}
		if (syncObject instanceof SyncMessage) {
			psMessage = (SyncMessage) syncObject;
			return true;
		}
		if (syncObject instanceof SyncCredentials) {
			psCredentials = (SyncCredentials) syncObject;
			return true;
		}
		return false;
	}

	public void beginTransaction() {
		inTransaction = true;
		psMessage.rc = 0;
		psMessage.message = "Ok";
		kdb4DX = null;
		progress("KeePassBB2 Sync Report");
		ApplicationManager appman = ApplicationManager.getApplicationManager();
		ApplicationDescriptor ad[] = appman.getVisibleApplications();
		boolean waitForApp = false;
		progress("Checking to see if app is running...");
		for (int i = 0; i < ad.length; i++)
			if (ad[i].getModuleName().toLowerCase().startsWith("keepassbb2"))
				waitForApp = true;
		if (waitForApp) {
			progress("It is.");
			appman.postGlobalEvent(GUID_SYNCSTART);
			RuntimeStore rs = RuntimeStore.getRuntimeStore();
			String p = (String) rs.waitFor(GUID_SYNCPROCEED);
			rs.remove(GUID_SYNCPROCEED);
			if (!p.equalsIgnoreCase("OK")) {
				progress("App told Sync to not proceed: " + p);
				throw new RuntimeException("App told Sync to not proceed: " + p);
			}
			progress("Saved database before synching.");
		} else
			progress("It isn't.");
	}

	public void clearSyncObjectDirty(SyncObject syncObject) {
		progress("clearSyncObjectDirty " + syncObject.getUID());
	}

	public void endTransaction() {
		inTransaction = false;
		CommonUtils.logger("Ending transaction.");
		if (psMessage.rc == RC_CHANGES_MADE || psMessage.rc == RC_RESTORE
				|| psMessage.rc == RC_LOADED_TO_DEVICE) {
			try {
				kdb4DX.save();
				progress("Database saved on device.");
			} catch (Exception e) {
				progress("Database error: " + e.getMessage());
				throw new RuntimeException(e.getMessage());
			} finally {
				kdb4DX = null;
			}
		}
		if (sbProgress != null)
			CommonUtils.logger(sbProgress.toString());
		else
			CommonUtils.logger("Transaction completed.");
		System.gc();
		ApplicationManager appman = ApplicationManager.getApplicationManager();
		appman.postGlobalEvent(GUID_SYNCCOMPLETE, 0, 0, "OK", null);
	}

	public SyncConverter getSyncConverter() {
		return this;
	}

	public String getSyncName() {
		return "KeePassBB2DatabaseObject.bak";
	}

	public String getSyncName(Locale locale) {
		return "KeePassBB2DatabaseObject.bak";
	}

	public SyncObject getSyncObject(int i) {
		/*
		if (Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE, false)) {
			progress("getSyncObject efm");

			if (inTransaction)
				return psMessage;
			else
				return null;
		}
		*/
		if (!inTransaction) {
			progress("getSyncObject NIT PwSyncObject");
			return new Kdb4SyncObject();
		}
		switch (i) {
		case (0):
			progress("getSyncObject PwSyncObject");
			return kdb4DX;
		case (1):
			progress("getSyncObject PwSyncMessage");
			return psMessage;
		}
		return (null);
	}

	public int getSyncObjectCount() {
		/*
		if (Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE, false)) {
			if (inTransaction) {
				progress("getSyncObjectCount 1");
				return 1;
			} else {
				progress("getSyncObjectCount 0");
				return 0;
			}
		}
		*/
		if (inTransaction) {
			progress("getSyncObjectCount 2");
			return 2;
		} else {
			progress("getSyncObjectCount 1");
			return 1;
		}
	}

	public SyncObject[] getSyncObjects() {
		/*
		if (Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE, false)) {
			if (inTransaction) {
				progress("getSyncObjects efm");
				return new SyncObject[] { psMessage };
			} else {
				return new SyncObject[0];
			}
		}
*/
		SyncObject psa[];
		if (!inTransaction) {
			progress(RC_BACKUP, "Backup started.");
			try {
				psa = new Kdb4SyncObject[1];
				psa[0] = new Kdb4SyncObject();
			} catch (Exception e) {
				psa = new Kdb4SyncObject[0];
			}
			return psa;
		}
		if (kdb4DX == null) {
			progress("getSyncObjects PwSyncMessage");
			psa = new SyncObject[] { psMessage };
		} else {
			progress("getSyncObjects PwSyncObject PwSyncMessage");
			psa = new SyncObject[] { kdb4DX, psMessage, };
		}
		return psa;
	}

	public int getSyncVersion() {
		progress("getSyncVersion");
		return 4;
	}

	public boolean isSyncObjectDirty(SyncObject object) {
		progress("isSyncObjectDirty " + object.getClass().getName() + ":"
				+ object.getUID());

		if (object instanceof SyncMessage) {
			return (true);
		}


		if (!inTransaction) {
			progress("isSyncObjectDirty Not In Transaction");
			return (true);
		}
		Kdb4SyncObject dboDesktop = (Kdb4SyncObject) object;
		Kdb4File kdb4Desktop;
		Kdb4File kdb4Device;

		try {
			if (!Kdb4File.checkForInternalDatabase()) {
				String msg = "No database was found on the device.  The desktop version will be loaded to "
						+ "the internal database"
						+ (psCredentials.keyfile != null ? " For security reasons, your keyfile must be transferred to the device manually. "
								: "");
				progress(RC_LOADED_TO_DEVICE, msg);
				return false;
			}
		} catch (Throwable e) {
			progress(RC_DEVICE_FILE_OPEN_EXCEPTION,
					"Exception opening the device database: " + e.toString());
			return false;
		}

		Kdb4KeyFile kf = null;
		if (psCredentials.keyfile != null)
			kf = new Kdb4KeyFile(psCredentials.keyfile);

		try {
			kdb4Device = new Kdb4File();
			kdb4Device.openInternal(psCredentials.password, kf);
		} catch (Throwable e) {
			progress(RC_DEVICE_DECRYPT_FAILURE,
					"Unable to decrypt device database using the supplied credentials");
			return false;
		}

		try {
			kdb4Desktop = new Kdb4File();
			kdb4Desktop.open(kdb4DX.db, psCredentials.password, kf);
		} catch (Throwable e) {
			progress(RC_DESKTOP_DECRYPT_FAILURE,
					"Unable to decrypt desktop database using the supplied credentials");
			return false;
		}

		boolean dirty = false;

		String deviceUUID = kdb4Device.kdb4.getRoot().getTopGroup().getUUID();
		String desktopUUID = kdb4Desktop.kdb4.getRoot().getTopGroup().getUUID();

		if (!deviceUUID.equalsIgnoreCase(desktopUUID)) {
			progress(
					RC_RECONCILE_EXCEPTION,
					"The device and desktop databases aren't derived from the same database.  The internal UUIDs don't match.");
			return false;
		}

		try {
			dirty = kdb4Device.kdb4.mergeInto(kdb4Desktop.kdb4)
					| kdb4Desktop.kdb4.mergeInto(kdb4Device.kdb4);
		} catch (Throwable e) {
			progress(RC_RECONCILE_EXCEPTION, "Exception while merging databases: "
					+ e.toString());
			return false;
		}

		if (dirty) {
			progress(RC_CHANGES_MADE, "Changes made");
			try {

				NoCopyByteArrayOutputStream baos = new NoCopyByteArrayOutputStream();
				kdb4Desktop.save(baos);
				dboDesktop.db = baos.getByteArray();
				dboDesktop.dbLength = baos.size();
			} catch (Throwable e) {
				progress(RC_DEVICE_FILE_SERIALIZE_EXCEPTION,
						"Exception while serializing database: " + e.toString());
				return false;
			}
		} else {
			progress(RC_NO_CHANGES_MADE, "No changed detected.");
		}
		progress("Sync successful.");
		return dirty;
	}

	public boolean removeSyncObject(SyncObject syncObject) {
		progress("RemoveSyncObject " + syncObject.getClass().getName() + ":"
				+ syncObject.getUID());
		return false;
	}

	public boolean removeAllSyncObjects() {
		progress(RC_RESTORE, "Restore started.");
		return false;
	}

	public void setSyncObjectDirty(SyncObject syncObject) {
		progress("setSyncObjectDirty " + syncObject.getUID());
	}

	public boolean updateSyncObject(SyncObject syncObject, SyncObject syncObject1) {
		progress("UpdateSyncObject " + syncObject.getClass().getName() + ":"
				+ syncObject.getUID() + ":" + syncObject1.getUID());
		return false;
	}

	public boolean convert(SyncObject syncObject, DataBuffer dataBuffer, int version) {
		progress("Convert " + syncObject.getClass().getName() + " -> DataBuffer");

		try {
			if (syncObject instanceof SyncMessage) {
				ConverterUtilities.writeInt(dataBuffer, FIELD_RECORD_TYPE,
						SyncMessage.UID);
				return ((SyncMessage) syncObject).convert(dataBuffer);
			}
			if (syncObject instanceof Kdb4SyncObject) {
				Kdb4SyncObject dbo = (Kdb4SyncObject) syncObject;
				ConverterUtilities.writeInt(dataBuffer, FIELD_RECORD_TYPE,
						Kdb4SyncObject.UID);
				return dbo.convert(dataBuffer);
			}
		} catch (Exception e) {
			progress(RC_CONVERT_OUT_ERROR, "KeePassBB convert OUT " + e.toString());
		}
		return false;
	}

	public SyncObject convert(DataBuffer dataBuffer, int i, int xuid) {
		if (i != this.getSyncVersion()) {
			throw new RuntimeException("INCOMPATABLE VERSION");
		}
		try {
			int uid = ConverterUtilities.readInt(dataBuffer);
			switch (uid) {
			case SyncCredentials.UID:
				progress("Convert PwSyncCredentials from buffer");
				return new SyncCredentials(dataBuffer);
			case Kdb4SyncObject.UID:
				progress("Convert PwSyncObject from buffer");
				return new Kdb4SyncObject(dataBuffer);
			}
		} catch (Throwable e) {
			progress(RC_CONVERT_IN_ERROR, "KeePassBB convert IN " + e.toString());
		}
		return null;
	}

	public class SyncMessage implements SyncObject {
		public static final int UID = 120;
		public static final int FIELD_RC = 121;
		public static final int FIELD_MSG = 122;
		public int rc = 990;
		public String message = "Ok";

		public int getUID() {
			return UID;
		}

		public boolean convert(DataBuffer dataBuffer) {
			ConverterUtilities.writeInt(dataBuffer, FIELD_RC, rc);
			ConverterUtilities.writeString(dataBuffer, FIELD_MSG, message);
			return true;
		}

	}

	public class SyncCredentials implements SyncObject {
		public static final int UID = 130;
		public static final int FIELD_PASSWORD = 131;
		public static final int FIELD_KEYFILE = 132;
		public String password;
		public byte[] keyfile;

		public SyncCredentials(DataBuffer dataBuffer) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int chunk = 0;
			
			while (dataBuffer.available() > 0) {
				int type = ConverterUtilities.getType(dataBuffer);
				switch (type) {
				case FIELD_PASSWORD:
					byte[] pba = ConverterUtilities.readByteArray(dataBuffer, true);
					password = new String(pba);
					break;
				case FIELD_KEYFILE:
					byte[] ba = ConverterUtilities.readByteArray(dataBuffer, true);
					baos.write(ba);
					chunk++;
					break;
				default:
					ConverterUtilities.skipField(dataBuffer);
				}
			}
			
			keyfile = baos.toByteArray();
			if (keyfile.length == 0) keyfile = null;
			baos.close();
			
			
		}

		public int getUID() {
			return UID;
		}
	}

}
