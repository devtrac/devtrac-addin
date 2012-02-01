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

package com.fairview5.keepassbb2;

import java.io.IOException;

import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;

import com.fairview5.keepassbb2.common.file.KFile;
import com.fairview5.keepassbb2.common.io.IOUtils;
import com.fairview5.keepassbb2.common.ui.EditDialog;
import com.fairview5.keepassbb2.common.ui.ProgressDialog;
import com.fairview5.keepassbb2.common.util.CommonUtils;
import com.fairview5.keepassbb2.kdb4.*;
import com.fairview5.keepassbb2.ui.*;

public final class KeePassBB2Screen extends MainScreen implements Runnable,
		GlobalEventListener {
	KeePassBB2 uiApp;
	TreeScreen tf;

	public KeePassBB2Screen(long style) {
		uiApp = (KeePassBB2) UiApplication.getUiApplication();
		uiApp.addGlobalEventListener(this);
		onExposed();
	}

	public void run() {
		Font.setDefaultFont(Options.getDefaultFont());
		try {
			tf = new TreeScreen(this);
			tf.setDefaultExpanded(Options.getBooleanOption(
					Options.OPTION_AUTOEXPANDGROUPS, false));
			add(tf);

			boolean externalMode = Options.getBooleanOption(
					Options.OPTION_EXTERNAL_FILE_MODE, false);
			boolean hasInternalDatabase = Kdb4File.checkForInternalDatabase();
			boolean autoReload = Options.getBooleanOption(
					Options.OPTION_AUTORELOAD, true);

			if (!externalMode) {
				if (!hasInternalDatabase) {
					importDatabase();
				} else {
					if (autoReload) {
						openInternalDatabase();
					}
				}
				return;
			} else {
				if (autoReload) {
					String db = Options.getStringOption(Options.OPTION_DEFAULTDBURL);
					if (db != null && db.length() > 0) {
						if (db.startsWith("http://") || db.startsWith("https://")) {
							this.openHttpDatabase(db);
						} else {
							openDatabase(db);
						}
					} else {
						openDatabase();
					}
				} else {
					openDatabase();
				}
			}
		} catch (Throwable e1) {
			Dialog.alert(e1.getMessage());
		}
	}

	public void setTitle() {
		if (Options.getBooleanOption(Options.OPTION_HIDETITLE, false))
			super.setTitle((Field) null);
		else {
			if (Kdb4File.current != null) {
				String fn = Kdb4File.current.fileName;
				if (fn == null)
					fn = "<unnameddb>";
				fn = fn + (isDirty() ? "*" : "");
				super.setTitle(new LabelField(fn, DrawStyle.ELLIPSIS
						| DrawStyle.TRUNCATE_BEGINNING));
			} else
				super.setTitle(KeePassBB2.name);
		}
	}

	private static String getBackdoor(int bd) {
		StringBuffer sb = new StringBuffer();
		char c = (char) ((bd >>> 24) & 0xff);
		if (c != 0)
			sb.append(c);
		c = (char) ((bd >>> 16) & 0xff);
		if (c != 0)
			sb.append(c);
		c = (char) ((bd >>> 8) & 0xff);
		if (c != 0)
			sb.append(c);
		c = (char) ((bd >>> 0) & 0xff);
		if (c != 0)
			sb.append(c);
		return sb.toString();
	}

	protected boolean openProductionBackdoor(int backdoorCode) {
		String bd = getBackdoor(backdoorCode);
		if (bd.equals("OPTN")) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					miOptions.run();
				}
			});
			return true; // handled
		}
		if (bd.equals("INTD")) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					delPersistentStore();
				}
			});
			return true; // handled
		}
		if (bd.equals("LGLG")) {
			EventLogger.startEventLogViewer();
			return true; // handled
		}

		return super.openProductionBackdoor(backdoorCode);
	}

	protected boolean onSavePrompt() {
		if (Options.getBooleanOption(Options.OPTION_AUTOSAVECLOSE, true)) {
			return onSave();
		}
		return super.onSavePrompt();
	}

	public void save() {
		if (!Kdb4File.current.isValid())
			return;

		try {
			ProgressDialog.prepareModal("Saving...");
			final Throwable[] ex = new Throwable[1];
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						if (Options.getBooleanOption(
								Options.OPTION_EXTERNAL_FILE_MODE, false)) {
							Kdb4File.current.save();
						} else {
							Kdb4File.current.saveInternal();
						}
						Kdb4File.current.kdb4.clearDirty();
						ProgressDialog.closeProgress();
					} catch (Throwable e) {
						ex[0] = e;
					}
				}
			});
			t.start();
			ProgressDialog.doModal();
			if (ex[0] != null)
				throw ex[0];
			setTitle();
		} catch (Throwable e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	public boolean isDirty() {
		return isManagerDirty();
	}

	protected void onExposed() {
		setTitle();
		// setFont(Options.getDefaultFont());
	}

	public boolean isManagerDirty() {
		if (Kdb4File.current == null)
			return false;
		return (Kdb4File.current.isDirty());
	}

	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE
				&& Options.getBooleanOption(Options.OPTION_ESCAPE, false)) {
			miHide.run();
			return (true);
		}
		return super.keyChar(c, status, time);
	}

	void addMI(boolean condition, Menu menu, MenuItem mi, int ordinal,
			int priority) {
		if (condition) {
			mi.setOrdinal(ordinal);
			mi.setPriority(priority);
			menu.add(mi);
			return;
		}
	}

	public void close() {
		if (Options.getBooleanOption(Options.OPTION_PROMPT_EXIT, false)) {
			int rc = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to exit?");
			if (rc != Dialog.YES) {
				return;
			}
		}
		super.close();
	}

	protected void makeContextMenu(ContextMenu ctx) {
	}

	public void makeMenu(Menu menu, int instance) {
		menu.deleteAll();
		tf.makeMenu(menu);

		menu.add(MenuItem.separator(500));

		addMI(true, menu, miNewDB, 1100, 0);
		addMI(true, menu, miOpenDB, 1110, 0);

		if (Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE, false)) {
			addMI(true, menu, miOpenURL, 1120, 0);
		} else {
			addMI(true, menu, miImportDB, 1130, 0);
			addMI(true, menu, miImportHttpDB, 1140, 0);
			addMI(Kdb4File.checkForInternalDatabase(), menu, miDeleteDB, 1150, 0);
			addMI(true, menu, miImportKeyFile, 1160, 0);
			addMI(Kdb4KeyFile.checkForInternalKeyfile(), menu, miDeleteKeyFile,
					1170, 0);
		}

		if (Kdb4File.current != null && Kdb4File.current.isValid()) {
			addMI(!Options.isReadonly(), menu, miMasterKey, 1200, 0);
			addMI(true, menu, miSave, 1210, 0);
			addMI(true, menu, miSaveAs, 1220, 0);
			addMI(true, menu, miClose, 1230, 0);
		}

		menu.add(MenuItem.separator(1250));
		addMI(true, menu, miHelp, 1300, 0);
		addMI((Kdb4File.current != null && Kdb4File.current.isValid())
				|| !Options.getBooleanOption(Options.OPTION_RESTRICTEDMODE, false),
				menu, miOptions, 1310, 0);
		addMI(true, menu, miAbout, 1320, 0);
		menu.add(MenuItem.separator(1350));
		addMI(isDirty(), menu, miDiscard, 1400, 0);
		addMI(
				Options
						.getIntOption(Options.OPTION_SAVETYPE, Options.SAVE_MANUALLY) != Options.SAVE_MANUALLY,
				menu, miHide, 1410, 0);

		addMI(true, menu, miExit, 1420, 0);
	}

	boolean promptedSave() {
		if (isDirty()) {
			int rc = Dialog.ask(Dialog.D_SAVE);
			if (rc == Dialog.SAVE) {
				save();
				return true;
			} else if (rc == Dialog.DISCARD) {
				return true;
			} else if (rc == Dialog.CANCEL) {
				return false;
			}
		}
		return true;
	}

	MenuItem miClose = new MenuItem("Close", 0, 0) {
		public void run() {
			if (promptedSave()) {
				if (Kdb4File.current != null)
					Kdb4File.current.close();
				tf.deleteAll();
				setTitle();
			}
		}
	};

	MenuItem miHelp = new MenuItem("Help", 0, 0) {
		public void run() {
			CommonUtils.showWebPage("/keepassbb2/");
		}
	};

	MenuItem miHide = new MenuItem("Hide", 0, 0) {
		public void run() {
			if (Options.getBooleanOption(Options.OPTION_AUTOSAVEHIDE, true)) {
				save();
			}
			UiApplication.getUiApplication().requestBackground();
		}
	};
	MenuItem miExit = new MenuItem("Exit", 0, 0) {
		public void run() {
			close();
		}
	};

	MenuItem miOpenDB = new MenuItem("Open Database", 0, 0) {
		public void run() {
			if (promptedSave())
				if (Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE,
						false)) {
					openDatabase();
				} else {
					openInternalDatabase();
				}
		}
	};

	MenuItem miImportDB = new MenuItem("Import DB from File", 0, 0) {
		public void run() {
			if (!promptedSave())
				return;
			importDatabase();
		}
	};

	MenuItem miImportHttpDB = new MenuItem("Import DB from URL", 0, 0) {
		public void run() {
			if (!promptedSave())
				return;
			try {
				importHttpDatabase();
			} catch (Throwable e1) {
				Dialog.alert(e1.getMessage());
			}
		}
	};

	MenuItem miOpenURL = new MenuItem("Open DB from URL", 0, 0) {
		public void run() {
			if (!promptedSave())
				return;
			openHttpDatabase();
		}
	};

	MenuItem miSaveAs = new MenuItem("Save As", 0, 0) {
		public void run() {
			KFile f = null;
			try {
				PathDialog pd = new PathDialog("Save As File", true);
				if (pd.doModal() != Dialog.OK)
					return;
				f = pd.getSelectedFileObject();
				Kdb4File.current.saveAs(f);
				setTitle();
			} catch (Throwable e1) {
				Dialog.alert(e1.getMessage());
			} finally {
				IOUtils.closeStream(f);
			}
		}
	};

	MenuItem miSave = new MenuItem("Save", 0, 0) {
		public void run() {
			try {
				save();
			} catch (Throwable e1) {
				Dialog.alert(e1.getMessage());
			}
		}
	};

	MenuItem miImportKeyFile = new MenuItem("Import Key File", 0, 0) {
		public void run() {
			try {
				KFile f;
				PathDialog pd = new PathDialog("Import Key File", false);
				if (pd.doModal() != Dialog.OK)
					return;
				f = pd.getSelectedFileObject();

				Kdb4KeyFile kf = new Kdb4KeyFile(f);
				kf.saveInternal();
				Status.show("Key File Imported");
			} catch (Throwable e1) {
				Dialog.alert(e1.getMessage());
			}
		}
	};

	MenuItem miDeleteKeyFile = new MenuItem("Delete Internal Keyfile", 0, 0) {
		public void run() {
			try {
				if (!Kdb4KeyFile.checkForInternalKeyfile()) {
					Dialog.alert("No keyfile to delete.");
					return;
				}
				PersistentStore
						.destroyPersistentObject(Kdb4KFPO.persistentStoreKey);
				Status.show("Keyfile deleted");
			} catch (Throwable e1) {
				Dialog.alert(e1.getMessage());
			}
		}
	};
	MenuItem miDeleteDB = new MenuItem("Delete Internal DB", 0, 0) {
		public void run() {
			try {
				delPersistentStore();
			} catch (Throwable e1) {
				Dialog.alert(e1.getMessage());
			}
		}
	};

	MenuItem miDiscard = new MenuItem("Discard Changes", 0, 0) {
		public void run() {
			if (Kdb4File.current.isValid())
				Kdb4File.current.kdb4.clearDirty();
			close();
		}
	};

	MenuItem miOptions = new MenuItem("Options", 0, 0) {
		public void run() {
			Options sc = new Options();
			uiApp.pushModalScreen(sc);
		}
	};

	MenuItem miAbout = new MenuItem("About", 0, 0) {
		public void run() {
			Status.show(KeePassBB2.name + " build " + KeePassBB2.version);
		}
	};

	MenuItem miMasterKey = new MenuItem("Change Master Key", 0, 0) {
		public void run() {
			changeMasterKey();
		}
	};

	MenuItem miNewDB = new MenuItem("New Database", 0, 0) {
		public void run() {
			if (!promptedSave())
				return;
			newDatabase();
		}
	};

	public void newDatabase() {
		final DatabasePropertiesDialog pd = new DatabasePropertiesDialog(6000);
		int rc = pd.doModal();
		if (rc != Dialog.OK)
			return;

		final boolean isExternal = Options.getBooleanOption(
				Options.OPTION_EXTERNAL_FILE_MODE, false);

		final PathDialog fd = new PathDialog("New Database", true);

		if (isExternal) {
			if (fd.doModal() != Dialog.OK)
				return;
		}

		final Kdb4File kfile = new Kdb4File();
		try {
			ProgressDialog.prepareModal("Creating New Database...");
			Thread t = new Thread() {
				public void run() {
					try {
						if (isExternal) {
							final KFile f;
							f = fd.getSelectedFileObject();

							kfile.newDatabase(f, pd.getPassword(), pd.getKeyFile(), pd
									.getKeyRounds());
							if (fd.getSetDefault()) {
								Options.setOption(Options.OPTION_AUTORELOAD, true);
								Options.setOption(Options.OPTION_DEFAULTDBURL, f
										.getFullName());
							}
						} else {
							Kdb4KeyFile kf = null;
							if (Kdb4KeyFile.checkForInternalKeyfile()) {
								kf = new Kdb4KeyFile();
								kf.loadInternal();
							}
							kfile.newDatabase(pd.getPassword(), kf, pd.getKeyRounds());
						}
						Kdb4File.current = kfile;
					} catch (Throwable e) {
						ProgressDialog.setInnerException(e);
					}
					ProgressDialog.closeProgress();
				}
			};
			t.start();
			ProgressDialog.doModal();
			ProgressDialog.conditionalThrow();
		} catch (Throwable e) {
			Dialog.alert("Error creating database: " + e.toString());
			return;
		}
		tf.deleteAll();
		Kdb4Root r = kfile.kdb4.getRoot();
		Kdb4Group tg = r.getTopGroup();
		tg.traverse(tf, 0, false);
		setTitle();

	}

	public void changeMasterKey() {
		final DatabasePropertiesDialog pd = new DatabasePropertiesDialog(
				(int) Kdb4File.current.keyEncryptionRounds);
		int rc = pd.doModal();
		if (rc != Dialog.OK)
			return;
		try {
			ProgressDialog.prepareModal("Preparing Master Key...");
			Thread t = new Thread() {
				public void run() {
					try {
						Kdb4KeyFile kf = pd.getKeyFile();
						if (Kdb4KeyFile.checkForInternalKeyfile()) {
							kf = new Kdb4KeyFile();
							kf.loadInternal();
						}
						Kdb4File.current.setTransformedKey(pd.getPassword(), kf, pd
								.getKeyRounds());
					} catch (Throwable th) {
						ProgressDialog.setInnerException(th);
					}
					ProgressDialog.closeProgress();
				}
			};
			t.start();
			ProgressDialog.doModal();
			ProgressDialog.conditionalThrow();
			Kdb4File.current.kdb4.setDirty();
		} catch (Throwable e1) {
			Dialog.alert("Cancelling");
		}
	}

	public boolean openDatabase() {
		KFile f = null;
		try {
			PathDialog pd = new PathDialog("Open Database", false);
			if (pd.doModal() != Dialog.OK)
				return false;
			f = pd.getSelectedFileObject();
			return openDatabase(f);
		} catch (Throwable e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			Dialog.alert(msg);
		} finally {
			IOUtils.closeStream(f);
		}
		return false;
	}

	public boolean openDatabase(String filename) {
		KFile f = null;
		try {
			f = new KFile(filename);
			return openDatabase(f);
		} catch (Throwable e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			Dialog.alert(msg + ": " + filename);
		} finally {
			IOUtils.closeStream(f);
		}
		return false;
	}

	public boolean openDatabase(final KFile f) {
		try {
			if (!f.exists())
				throw new FileNotFoundException("File not found: "
						+ f.getFullName());

			final PasswordDialog pd = new PasswordDialog();
			int rc = pd.doModal();
			if (rc != Dialog.OK)
				return false;

			final Kdb4File k4file = new Kdb4File();
			ProgressDialog.prepareModal("Opening");
			Thread t = new Thread() {
				public void run() {

					try {
						k4file.open(f, pd.getPassword(), pd.getKeyFile());
						Kdb4File.current = k4file;
					} catch (Throwable e) {
						ProgressDialog.setInnerException(e);
					} finally {
						ProgressDialog.closeProgress();
					}
				}
			};
			t.start();
			ProgressDialog.doModal();
			ProgressDialog.conditionalThrow();
			tf.deleteAll();
			Kdb4Root r = k4file.kdb4.getRoot();
			Kdb4Group tg = r.getTopGroup();
			tg.traverse(tf, 0, false);
			setTitle();
		} catch (Throwable e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			Dialog.alert(msg);
			return false;
		}
		return true;
	}

	public boolean openInternalDatabase() {
		try {

			final PasswordDialog pd = new PasswordDialog();
			int rc = pd.doModal();
			if (rc != Dialog.OK)
				return false;

			final Kdb4File k4file = new Kdb4File();
			ProgressDialog.prepareModal("Opening...");
			Thread t = new Thread() {
				public void run() {

					try {
						Kdb4KeyFile kf = null;
						if (Kdb4KeyFile.checkForInternalKeyfile()) {
							kf = new Kdb4KeyFile();
							kf.loadInternal();
						}
						k4file.openInternal(pd.getPassword(), kf);
						Kdb4File.current = k4file;
					} catch (Throwable e) {
						ProgressDialog.setInnerException(e);
					} finally {
						ProgressDialog.closeProgress();
					}
				}
			};
			t.start();
			ProgressDialog.doModal();
			ProgressDialog.conditionalThrow();
			tf.deleteAll();
			Kdb4Root r = k4file.kdb4.getRoot();
			Kdb4Group tg = r.getTopGroup();
			tg.traverse(tf, 0, false);
			setTitle();
		} catch (Throwable e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			Dialog.alert(msg);
			return false;
		}
		return true;
	}

	public boolean importDatabase() {
		KFile f = null;
		try {

			if (Kdb4File.checkForInternalDatabase()) {
				int rc = Dialog.ask(Dialog.D_OK_CANCEL,
						"Overwrite the current internal database?");
				if (rc == Dialog.CANCEL)
					return false;
			}

			PathDialog pd = new PathDialog("Import Database", false);
			if (pd.doModal() != Dialog.OK)
				return false;
			f = pd.getSelectedFileObject();
			if (f.isRoot()) {
				Dialog.alert("No file selected.");
				return false;
			}
			Kdb4PO kpo = new Kdb4PO();
			kpo.ba = f.readContents();
			kpo.size = kpo.ba.length;
			PersistentObject po = PersistentStore
					.getPersistentObject(Kdb4PO.persistentStoreKey);
			po.setContents(kpo);
			po.forceCommit();
			openInternalDatabase();
		} catch (Throwable e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			Dialog.alert(msg);
		} finally {
			IOUtils.closeStream(f);
		}
		return false;
	}

	void importHttpDatabase() throws IOException {
		if (Kdb4File.checkForInternalDatabase()) {
			int rc = Dialog.ask(Dialog.D_OK_CANCEL,
					"Overwrite the current internal database?");
			if (rc == Dialog.CANCEL)
				return;
		}

		String url = Options.getLastUsedURL();
		EditDialog ed = new EditDialog("URL:", new EditField(null, url, 256,
				EditField.FILTER_URL));
		if (ed.doModal() != Dialog.OK)
			return;
		Options.setLastUsedURL(ed.getText());
		boolean ds = Options.getBooleanOption(Options.OPTION_DISABLE_MDS, false);

		Kdb4PO kpo = new Kdb4PO();

		kpo.ba = CommonUtils.getHttpFile(ed.getText()
				+ (ds ? ";deviceside=true" : ""));

		kpo.size = kpo.ba.length;
		PersistentObject po = PersistentStore
				.getPersistentObject(Kdb4PO.persistentStoreKey);
		po.setContents(kpo);
		po.forceCommit();
		openInternalDatabase();
	}

	void openHttpDatabase() {
		String url = Options.getLastUsedURL();
		EditDialog ed = new EditDialog("HTTP URL:", new EditField(null, url, 256,
				EditField.FILTER_URL));
		if (ed.doModal() != Dialog.OK)
			return;
		Options.setLastUsedURL(ed.getText());
		openHttpDatabase(ed.getText());
	}

	void openHttpDatabase(String url) {
		try {
			boolean ds = Options.getBooleanOption(Options.OPTION_DISABLE_MDS,
					false);

			byte[] ba = CommonUtils.getHttpFile(url
					+ (ds ? ";deviceside=true" : ""));

			String fn = "/store/home/user/keepass/httpdb.kdbx";

			KFile kf = new KFile(fn);
			if (kf.exists())
				kf.truncate();
			else
				kf.create(true);
			kf.write(ba);
			IOUtils.closeStream(kf);

			openDatabase(fn);

		} catch (Throwable e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			Dialog.alert(msg);
		}

	}

	public void eventOccurred(long guid, int i, int i1, Object object,
			Object object1) {

		if (guid == Kdb4Synchronizer.GUID_SYNCCOMPLETE) {
			uiApp.invokeLater(new Runnable() {
				public void run() {
					Status.show("Sync Completed", 2000);
					try {
						// reloadDatabase();
					} catch (Throwable e1) {
						Dialog.alert(e1.getMessage());
					}
				}
			});
		} else if (guid == Kdb4Synchronizer.GUID_SYNCSTART) {
			RuntimeStore rt = RuntimeStore.getRuntimeStore();
			try {
				if (isManagerDirty())
					save();
				rt.put(Kdb4Synchronizer.GUID_SYNCPROCEED, "OK");
				close();
			} catch (RuntimeException e) {
				rt.put(Kdb4Synchronizer.GUID_SYNCPROCEED, e.toString());
			}

		}

	}

	void delPersistentStore() {
		try {
			int rc = Dialog.ask(Dialog.D_DELETE,
					"Are you sure you want to delete the internal database?");
			if (rc != Dialog.DELETE)
				return;
			miClose.run();
			PersistentStore.destroyPersistentObject(Kdb4PO.persistentStoreKey);

			Status.show("Internal database deleted");
		} catch (Throwable e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0)
				msg = e.toString();
			Dialog.alert(msg);
		}
	}

}
