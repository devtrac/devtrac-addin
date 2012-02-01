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

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.synchronization.SyncItem;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.SimpleSortingIntVector;

import com.fairview5.keepassbb2.common.ui.BooleanChoiceField;
import com.fairview5.keepassbb2.common.ui.PathWidget;
import com.fairview5.keepassbb2.common.util.CommonUtils;
import com.fairview5.keepassbb2.common.util.TypedIntHashtable;
import com.fairview5.keepassbb2.kdb4.Kdb4Synchronizer;

public final class Options extends MainScreen implements FieldChangeListener,
		FocusChangeListener {

	static PersistentObject store;
	static TypedIntHashtable opts;

	// -----=====-----=====-----=====--
	// 20012222222222112220122102220022

	// private static int typeArray[] = {
	// 2,0,0,1,2,2,2,2,2,2,2,2,2,2,1,1,2,2,2,0,1,2,2,1,0,2,2,2,0,0,2,2 };

	public static final int OPTION_VERSION = -1;
	public static final int VERSION = 3;

	public static final int OPTION_AUTORELOAD = 0;
	BooleanChoiceField cbAutoReload;
	public static final int OPTION_DEFAULTDBURL = 1;
	PathWidget efDefaultDBURL;
	LabelField lfDefaultDBURL;
	public static final int OPTION_LASTUSEDURL = 2;
	String lastUsedURL;
	public static final int OPTION_TIMEOUT = 3;
	ObjectChoiceField ofTimeout;
	public static final int OPTION_ESCAPE = 4;
	BooleanChoiceField cbEscape;

	public static final int OPTION_HOLSTER = 5;
	BooleanChoiceField cbHolster;
	public static final int OPTION_SINGLECLICKGROUP = 6;
	BooleanChoiceField cbExpandGroup;
	public static final int OPTION_SINGLECLICKENTRY = 7;
	BooleanChoiceField cbExpandEntry;
	public static final int OPTION_AUTOEXPANDGROUPS = 8;
	BooleanChoiceField cbAutoExpand;
	public static final int OPTION_READONLY = 9;
	BooleanChoiceField cbReadOnly;

	public static final int OPTION_SAVELASTACCESS = 10;
	BooleanChoiceField cbSaveLastAccess;
	public static final int OPTION_AUTOACCEPT = 11;
	BooleanChoiceField cbAutoAccept;
	public static final int OPTION_DELETEPROMPT = 12;
	BooleanChoiceField cbDeletePrompt;
	public static final int OPTION_AUTOSAVELASTACCESS = 13;
	BooleanChoiceField cbAutoSaveLastAccess;
	public static final int OPTION_SAVETYPE = 14;
	ObjectChoiceField ofSaveType;

	public static final int OPTION_SEARCHTYPE = 15;
	ObjectChoiceField ofSearchType;
	public static final int OPTION_HIDETITLE = 16;
	BooleanChoiceField cbHideTitle;
	public static final int OPTION_HIDEICONS = 17;
	BooleanChoiceField cbHideIcons;
	public static final int OPTION_REMEMBERURLS = 18;
	BooleanChoiceField cbRememberUrls;
	public static final int OPTION_FONT_FAMILY = 19;
	ObjectChoiceField ofFontFamily;

	public static final int OPTION_FONT_SIZE = 20;
	ObjectChoiceField ofFontSize;
	public static final int OPTION_HIDEENTRYURL = 21;
	BooleanChoiceField cbHideEntryUrl;
	public static final int OPTION_HIDEENTRYNOTETITLE = 22;
	BooleanChoiceField cbHideEntryNoteTitle;
	public static final int OPTION_NOTUSED23 = 23;
	int notUsed23;
	public static final int OPTION_DEFAULTKEYFILEURL = 24;
	PathWidget efDefaultKeyfileURL;

	public static final int OPTION_HIDEKEYFILEPROMPT = 25;
	BooleanChoiceField cbKeyfilePrompt;
	public static final int OPTION_SEARCHFYAT = 26;
	BooleanChoiceField cbSearchFYAT;
	public static final int OPTION_KEYFILERELOAD = 27;
	BooleanChoiceField cbKeyfileReload;
	public static final int OPTION_LASTUSEDFILE = 28;
	String lastUsedFile;
	public static final int OPTION_LASTUSEDDIR = 29;
	String lastUsedDir;

	public static final int OPTION_TIMEOUTACTION = 30;
	BooleanChoiceField cbTimeoutAction;
	public static final int OPTION_RESTRICTEDMODE = 31;
	BooleanChoiceField cbRestrictedMode;
	public static final int OPTION_AUTOSAVECHANGES = 32;
	BooleanChoiceField cbAutoSaveChanges;
	public static final int OPTION_AUTOSAVECLOSE = 33;
	BooleanChoiceField cbAutoSaveClose;
	public static final int OPTION_AUTOSAVEHIDE = 34;
	BooleanChoiceField cbAutoSaveHide;
	public static final int OPTION_DISABLE_MDS = 35;
	BooleanChoiceField cbDisableMDS;
	public static final int OPTION_EXTERNAL_FILE_MODE = 36;
	BooleanChoiceField cbExternalFileMode;
	public static final int OPTION_SHOW_USERID_IN_TREE = 37;
	BooleanChoiceField cbShowUseridInTree;
	public static final int OPTION_SORT_TREE = 38;
	BooleanChoiceField cbSortTree;
	public static final int OPTION_SHOW_GROUP = 39;
	BooleanChoiceField cbShowGroup;
	public static final int OPTION_PROMPT_EXIT = 40;
	BooleanChoiceField cbPromptExit;
	public static final int OPTION_SHOW_GROUP_SEARCH = 41;
	BooleanChoiceField cbShowGroupSearch;
	public static final int OPTION_HIDE_EMPTY_FIELDS = 42;
	BooleanChoiceField cbHideEmptyFields;
	public static final int OPTION_TAG_EXPIRED_ENTRIES = 43;
	BooleanChoiceField cbTagExpiredEntries;

	public static final int OPTION_LAST = 43;

	public static final int SAVE_EVERYCHANGE = 0;
	public static final int SAVE_APPCLOSE = 1;
	public static final int SAVE_MANUALLY = 2;

	public static final int SEARCH_TITLESTARTSWITH = 0;
	public static final int SEARCH_ANYFIELDCONTAINS = 1;

	public static final int KEYFILE_NEVER = 0;
	public static final int KEYFILE_STORE = 1;
	public static final int KEYFILE_FILESYSTEM = 2;

	public static final int OPTION_TYPE_STRING = 0;
	public static final int OPTION_TYPE_INT = 1;
	public static final int OPTION_TYPE_BOOLEAN = 2;

	static String[] timeoutStrings = { "Disabled", "10s", "20s", "30s", "40s",
			"50s", "1m", "1m 30s", "2m", "2m 30s", "3m", "3m 30s", "4m", "4m 40s",
			"5m" };
	static int[] timeouts = { 0, 10, 20, 30, 40, 50, 60, 90, 120, 150, 180, 210,
			240, 270, 300 };

	static String[] saveChoices = { "After Every Change", "When the App Closes",
			"Manually" };
	static String[] searchChoices = { "Title Starts With", "Any Field Contains" };

	static final long GUID_OPTIONS = CommonUtils.createGUID("GUID_OPTIONS");
	static final long STORE_UID_OLD = 0xbe2e01d9953b3679L;

	static {
		store = PersistentStore.getPersistentObject(GUID_OPTIONS);
		// key is hash of com.fairview5.keepassbb.Options
		Object o = store.getContents();
		synchronized (store) {
			if (o == null || !(o instanceof TypedIntHashtable)) {
				o = new TypedIntHashtable();
				store.setContents(o);
				store.commit();
			}
			opts = (TypedIntHashtable) o;

		}
	}

	ButtonField bfBrowse;
	LabelField lfFontSample;

	private BooleanChoiceField addBooleanChoiceField(int option, boolean def,
			String label, boolean visible) {
		boolean checked = getBooleanOption(option, def);
		return new BooleanChoiceField(label, checked);
	}

	private BooleanChoiceField addBooleanChoiceField(int option, boolean def,
			String label) {
		return addBooleanChoiceField(option, def, label, true);
	}

	Options() {
		if (Options.getBooleanOption(Options.OPTION_HIDETITLE, false))
			setTitle((Field) null);
		else
			setTitle("KeePassBB Options");

		FontFamily[] ffa = FontFamily.getFontFamilies();
		FontFamily dff = Font.getDefault().getFontFamily();
		String ffam = getStringOption(OPTION_FONT_FAMILY, dff.getName());
		String[] famnames = new String[ffa.length];
		int ffix = 0;
		for (int i = 0; i < ffa.length; i++) {
			if (ffa[i].getName().equals(ffam))
				ffix = i;
			famnames[i] = ffa[i].getName();
		}

		ofFontFamily = new ObjectChoiceField("Base Font Family", famnames, ffix);
		ofFontFamily.setChangeListener(this);
		ofFontFamily.setFocusListener(this);

		int dfpt = Ui.convertSize(Font.getDefault().getHeight(), Ui.UNITS_px,
				Ui.UNITS_pt);
		int fhpt = getIntOption(OPTION_FONT_SIZE, dfpt);
		int[] fsa = ffa[ffix].getHeights();
		int fhix = 0;
		SimpleSortingIntVector v = new SimpleSortingIntVector();
		for (int i = 0; i < fsa.length; i++) {
			int fz = Ui.convertSize(fsa[i], Ui.UNITS_px, Ui.UNITS_pt);
			if (!v.contains(fz)) {
				v.addElement(fz);
				if (fz == fhpt)
					fhix = v.indexOf(fz);
			}
		}
		Integer[] ia = new Integer[v.size()];
		for (int i = 0; i < ia.length; i++)
			ia[i] = new Integer(v.elementAt(i));
		ofFontSize = new ObjectChoiceField("Base Font Size", ia, fhix);
		ofFontSize.setChangeListener(this);
		ofFontSize.setFocusListener(this);

		lfFontSample = new LabelField(
				"The quick brown fox jumps over the lazy dog's back.");
		lfFontSample.setFont(getDefaultFont());

		String dvalue = null;

		cbAutoReload = addBooleanChoiceField(OPTION_AUTORELOAD, true,
				"Reload Database on Startup:");
		cbAutoReload.setChangeListener(this);

		dvalue = getStringOption(OPTION_DEFAULTDBURL);
		// if (dvalue == null)
		// dvalue = getStringOption(OPTION_SAVEDURL);
		efDefaultDBURL = new PathWidget(null, dvalue);
		lfDefaultDBURL = new LabelField(
				"Specify a Database for Reload on Startup:");

		int ix = getIntOption(OPTION_TIMEOUT, 0);
		if (ix > timeouts.length)
			ix = 3;
		ofTimeout = new ObjectChoiceField("Inactivity timeout:", timeoutStrings,
				ix);
		ofTimeout.setChangeListener(this);
		cbEscape = addBooleanChoiceField(OPTION_ESCAPE, false,
				"Escape Hides KeePassBB:");
		cbEscape.setChangeListener(this);

		cbHolster = addBooleanChoiceField(OPTION_HOLSTER, false,
				"Close When Holstered:");
		cbHolster.setChangeListener(this);

		cbExpandGroup = addBooleanChoiceField(OPTION_SINGLECLICKGROUP, false,
				"Single click opens/closes group:");

		cbExpandEntry = addBooleanChoiceField(OPTION_SINGLECLICKENTRY, false,
				"Single click opens entry:");

		cbAutoExpand = addBooleanChoiceField(OPTION_AUTOEXPANDGROUPS, false,
				"Expand Groups on Start:");

		cbReadOnly = addBooleanChoiceField(OPTION_READONLY, false,
				"Read Only Mode:");

		ix = getIntOption(OPTION_SEARCHTYPE, 0);
		if (ix > searchChoices.length)
			ix = SEARCH_TITLESTARTSWITH;
		ofSearchType = new ObjectChoiceField("Search Type:", searchChoices, ix);

		cbSaveLastAccess = addBooleanChoiceField(OPTION_SAVELASTACCESS, true,
				"Save 'Last Access' Changes:");

		cbAutoSaveLastAccess = addBooleanChoiceField(OPTION_AUTOSAVELASTACCESS,
				true, "Auto Save 'Last Access' Changes:", false);

		/*
		 * ix = getIntOption(OPTION_SAVETYPE, 0); if (ix > saveChoices.length) ix
		 * = SAVE_EVERYCHANGE; ofSaveType = new
		 * ObjectChoiceField(KeePassBB.getString
		 * (KeePassBB2Resource.OPTION_SAVETYPE), saveChoices, ix);
		 * ofSaveType.setChangeListener(this);
		 */
		cbAutoSaveChanges = addBooleanChoiceField(OPTION_AUTOSAVECHANGES, true,
				"Auto Save Every Change:");
		cbAutoSaveClose = addBooleanChoiceField(OPTION_AUTOSAVECLOSE, true,
				"Auto Save on App Close:");
		cbAutoSaveHide = addBooleanChoiceField(OPTION_AUTOSAVEHIDE, true,
				"Auto Save on App Hide:");

		cbAutoAccept = addBooleanChoiceField(OPTION_AUTOACCEPT, false,
				"Auto Confirm Entry Changes:", false);

		cbDeletePrompt = addBooleanChoiceField(OPTION_DELETEPROMPT, true,
				"Prompt on Delete:");

		cbHideIcons = addBooleanChoiceField(OPTION_HIDEICONS, false,
				"Hide Tree Icons:");
		cbHideTitle = addBooleanChoiceField(OPTION_HIDETITLE, false,
				"Hide Title Bar:");
		cbRememberUrls = addBooleanChoiceField(OPTION_REMEMBERURLS, true,
				"Remember Last URLs:");

		cbHideEntryUrl = addBooleanChoiceField(OPTION_HIDEENTRYURL, false,
				"Hide Entry URL:");
		cbHideEntryNoteTitle = addBooleanChoiceField(OPTION_HIDEENTRYNOTETITLE,
				false, "Hide Entry Note Title:");

		cbKeyfilePrompt = addBooleanChoiceField(OPTION_HIDEKEYFILEPROMPT, false,
				"Hide Keyfile on Password Prompt:");
		cbKeyfileReload = addBooleanChoiceField(OPTION_KEYFILERELOAD, false,
				"Load Keyfile on Startup:");
		efDefaultKeyfileURL = new PathWidget(null,
				getStringOption(OPTION_DEFAULTKEYFILEURL));

		cbSearchFYAT = addBooleanChoiceField(OPTION_SEARCHFYAT, true,
				"Find as You Type:");
		cbRestrictedMode = addBooleanChoiceField(OPTION_RESTRICTEDMODE, false,
				"Restricted Mode:");
		cbDisableMDS = addBooleanChoiceField(OPTION_DISABLE_MDS, false,
				"Disable MDS for HTTP Requests:");
		cbExternalFileMode = addBooleanChoiceField(OPTION_EXTERNAL_FILE_MODE,
				false, "Use External File Mode:");
		cbExternalFileMode.setChangeListener(this);

		cbShowUseridInTree = addBooleanChoiceField(OPTION_SHOW_USERID_IN_TREE,
				false, "Show Userid in the Tree:");

		cbSortTree = addBooleanChoiceField(OPTION_SORT_TREE, false,
				"Sort the Tree:");

		cbShowGroup = addBooleanChoiceField(OPTION_SHOW_GROUP, false,
				"Show Parent Group in Entry Details:");
		cbPromptExit = addBooleanChoiceField(OPTION_PROMPT_EXIT, false,
				"Prompt Before Exit:");
		cbShowGroupSearch = addBooleanChoiceField(OPTION_SHOW_GROUP_SEARCH,
				false, "Show Parent Group in Search Results:");

		cbHideEmptyFields = addBooleanChoiceField(OPTION_HIDE_EMPTY_FIELDS,
				false, "Hide Empty Entry Fields:");

		cbTagExpiredEntries = addBooleanChoiceField(OPTION_TAG_EXPIRED_ENTRIES,
				false, "Tag Expired Entries:");

		add(new BoldLabelField("User Interface", Field.FIELD_HCENTER));
		add(ofFontFamily);
		add(ofFontSize);
		add(lfFontSample);
		add(cbShowUseridInTree);
		add(cbSortTree);
		add(cbHideTitle);
		add(cbHideIcons);
		add(cbHideEmptyFields);
		add(cbTagExpiredEntries);
		// add(cbHideEntryUrl);
		// add(cbHideEntryNoteTitle);
		add(cbShowGroup);
		add(cbShowGroupSearch);
		add(cbDeletePrompt);
		// add(ofSearchType);
		add(cbSearchFYAT);
		add(cbAutoExpand);
		add(cbAutoAccept);
		add(cbPromptExit);

		// add(new SeparatorField());
		// add(new BoldLabelField("Startup Options", Field.FIELD_HCENTER));

		add(new SeparatorField());
		add(new BoldLabelField("Security", Field.FIELD_HCENTER));
		add(ofTimeout);
		add(cbEscape);
		add(cbHolster);
		add(cbRestrictedMode);
		add(cbRememberUrls);

		add(new SeparatorField());
		add(new BoldLabelField("Database", Field.FIELD_HCENTER));
		add(cbReadOnly);
		add(cbSaveLastAccess);
		add(cbAutoSaveChanges);
		add(cbAutoSaveClose);
		add(cbAutoSaveHide);
		add(cbAutoReload);
		add(cbExternalFileMode);
		if (cbExternalFileMode.getChecked()) {
			add(lfDefaultDBURL);
			add(efDefaultDBURL);
			// add(new SeparatorField());
			// add(new
			// BoldLabelField(KeePassBB2.getString(KeePassBB2Resource.OPTION_HEADER_KEYFILE),
			// Field.FIELD_HCENTER));
			add(cbKeyfilePrompt);
			add(cbKeyfileReload);
			add(efDefaultKeyfileURL);
		}

		add(new SeparatorField());
		add(new BoldLabelField("Advanced Options", Field.FIELD_HCENTER));
		add(cbDisableMDS);

	}

	public void makeMenu(Menu km, int instance) {
		if (isDirty()) {
			km.add(new MenuItem("Save", 90, 500) {
				public void run() {
					save();
					setDirty(false);
				}
			});
			km.add(new MenuItem("Discard Changes", 100, 500) {
				public void run() {
					setDirty(false);
					close();
				}
			});
		}
		km.add(new MenuItem("Reset to Defaults", 110, 500) {
			public void run() {
				int rc = Dialog.ask(Dialog.D_YES_NO, "Are you sure?");
				if (rc != Dialog.YES)
					return;

				synchronized (store) {
					opts = new TypedIntHashtable();
					store.setContents(opts);
					store.commit();
				}
				setDirty(false);
				close();
			}
		});

		MenuItem misep1 = MenuItem.separator(120);
		km.add(misep1);
		km.add(new MenuItem("Show System Event Log", 119, 500) {
			public void run() {
				EventLogger.startEventLogViewer();
			}
		});

		km.add(new MenuItem("Help", 120, 500) {
			public void run() {
				CommonUtils.showWebPage("/keepassbb2/");
			}
		});
		km.add(MenuItem.separator(120));
		km.add(new MenuItem("Close", 130, 500) {
			public void run() {
				close();
			}
		});

	}

	public void focusChanged(Field field, int eventType) {
		if (field == ofFontFamily || field == ofFontSize) {
			int ix = ofFontFamily.getSelectedIndex();
			String fname = (String) ofFontFamily.getChoice(ix);
			ix = ofFontSize.getSelectedIndex();
			int fs = ((Integer) ofFontSize.getChoice(ix)).intValue();
			FontFamily fam = null;
			try {
				fam = FontFamily.forName(fname);
				Font f = fam.getFont(Font.PLAIN, fs, Ui.UNITS_pt);
				lfFontSample.setFont(f);
			} catch (ClassNotFoundException e) {
			}
		}
	}

	public static Font getDefaultFont() {
		if (!isOptionSet(OPTION_FONT_FAMILY)) {
			return Font.getDefault();
		}
		FontFamily dff = Font.getDefault().getFontFamily();
		String ffam = getStringOption(OPTION_FONT_FAMILY, dff.getName());
		FontFamily fam = null;
		try {
			fam = FontFamily.forName(ffam);
		} catch (ClassNotFoundException e) {
		}

		int dfh = Ui.convertSize(Font.getDefault().getHeight(), Ui.UNITS_px,
				Ui.UNITS_pt);
		int fpt = getIntOption(OPTION_FONT_SIZE, dfh);

		Font f = fam.getFont(Font.PLAIN, fpt, Ui.UNITS_pt);
		return (f);
	}

	public static Font getMenuActionFont() {
		Font f = getDefaultFont();
		int fh = f.getHeight();
		Font nf = f.derive(Font.PLAIN, fh - 1);
		return nf;
	}

	public static Font getMenuHeaderFont() {
		Font f = getDefaultFont();
		int fh = f.getHeight();
		Font nf = f.derive(Font.ITALIC, fh - 2);
		return nf;
	}

	public boolean zzisDataValid() {
		if (ofSaveType.getSelectedIndex() == SAVE_MANUALLY
				&& (ofTimeout.getSelectedIndex() != 0 || cbEscape.getChecked() || cbHolster
						.getChecked())) {
			Dialog
					.alert("Save Changes must not be 'Manually' if Timeout, Close When Holstered, or Escape Hides KeePassBB is turned on.");
			return false;
		}
		return true;
	}

	public void save() {

		boolean intmode = !getBooleanOption(OPTION_EXTERNAL_FILE_MODE, false);

		opts.put(OPTION_AUTORELOAD, cbAutoReload.getChecked());
		opts.put(OPTION_DEFAULTDBURL, efDefaultDBURL.getText());
		opts.put(OPTION_TIMEOUT, ofTimeout.getSelectedIndex());
		opts.put(OPTION_ESCAPE, cbEscape.getChecked());
		opts.put(OPTION_HOLSTER, cbHolster.getChecked());
		opts.put(OPTION_SINGLECLICKGROUP, cbExpandGroup.getChecked());
		opts.put(OPTION_SINGLECLICKENTRY, cbExpandEntry.getChecked());
		opts.put(OPTION_AUTOEXPANDGROUPS, cbAutoExpand.getChecked());
		opts.put(OPTION_READONLY, cbReadOnly.getChecked());
		opts.put(OPTION_SAVELASTACCESS, cbSaveLastAccess.getChecked());
		opts.put(OPTION_AUTOACCEPT, cbAutoAccept.getChecked());
		opts.put(OPTION_DELETEPROMPT, cbDeletePrompt.getChecked());
		opts.put(OPTION_AUTOSAVELASTACCESS, cbAutoSaveLastAccess.getChecked());
		opts.put(OPTION_AUTOSAVECHANGES, cbAutoSaveChanges.getChecked());
		opts.put(OPTION_AUTOSAVECLOSE, cbAutoSaveClose.getChecked());
		opts.put(OPTION_AUTOSAVEHIDE, cbAutoSaveHide.getChecked());
		opts.put(OPTION_SEARCHTYPE, ofSearchType.getSelectedIndex());

		opts.put(OPTION_FONT_FAMILY, ofFontFamily.getChoice(ofFontFamily
				.getSelectedIndex()));
		opts.put(OPTION_FONT_SIZE, ((Integer) ofFontSize.getChoice(ofFontSize
				.getSelectedIndex())).intValue());
		opts.put(OPTION_HIDEICONS, cbHideIcons.getChecked());
		opts.put(OPTION_HIDETITLE, cbHideTitle.getChecked());
		opts.put(OPTION_REMEMBERURLS, cbRememberUrls.getChecked());
		opts.put(OPTION_HIDEENTRYURL, cbHideEntryUrl.getChecked());
		opts.put(OPTION_HIDEENTRYNOTETITLE, cbHideEntryNoteTitle.getChecked());

		opts.put(OPTION_HIDEKEYFILEPROMPT, cbKeyfilePrompt.getChecked());
		opts.put(OPTION_KEYFILERELOAD, cbKeyfileReload.getChecked());
		opts.put(OPTION_DEFAULTKEYFILEURL, efDefaultKeyfileURL.getText());
		opts.put(OPTION_RESTRICTEDMODE, cbRestrictedMode.getChecked());
		opts.put(OPTION_SEARCHFYAT, cbSearchFYAT.getChecked());
		opts.put(OPTION_DISABLE_MDS, cbDisableMDS.getChecked());
		opts.put(OPTION_EXTERNAL_FILE_MODE, cbExternalFileMode.getChecked());
		opts.put(OPTION_SHOW_USERID_IN_TREE, cbShowUseridInTree.getChecked());
		opts.put(OPTION_SORT_TREE, cbSortTree.getChecked());
		opts.put(OPTION_SHOW_GROUP, cbShowGroup.getChecked());
		opts.put(OPTION_PROMPT_EXIT, cbPromptExit.getChecked());
		opts.put(OPTION_SHOW_GROUP_SEARCH, cbShowGroupSearch.getChecked());
		opts.put(OPTION_HIDE_EMPTY_FIELDS, cbHideEmptyFields.getChecked());
		opts.put(OPTION_TAG_EXPIRED_ENTRIES, cbTagExpiredEntries.getChecked());

		commitPersistentStore();
		Font.setDefaultFont(getDefaultFont());

		boolean intmode2 = !getBooleanOption(OPTION_EXTERNAL_FILE_MODE, false);

		if (intmode != intmode2) {
			Kdb4Synchronizer k = Kdb4Synchronizer.getMe();
			if (intmode2) {
				if (k == null) {
					k = new Kdb4Synchronizer();
					k.enableSync();
					CommonUtils.logger("Enabling database sync");
				}
			} else {
				if (k != null) {
					k.disableSync();
					CommonUtils.logger("Disabling database sync");
				}
			}

		}
	}

	public void fieldChanged(Field field, int ixx) {
		focusChanged(field, ixx);

		if (field == cbExternalFileMode) {
			if (!cbExternalFileMode.getChecked()) {
				delete(efDefaultDBURL);
				delete(lfDefaultDBURL);
				delete(cbKeyfilePrompt);
				delete(cbKeyfileReload);
				delete(efDefaultKeyfileURL);

			} else {
				int ix = cbExternalFileMode.getIndex() + 1;
				insert(lfDefaultDBURL, ix++);
				insert(efDefaultDBURL, ix++);
				insert(cbKeyfilePrompt, ix++);
				insert(cbKeyfileReload, ix++);
				insert(efDefaultKeyfileURL, ix++);
			}
		}
		isDataValid();
	}

	public static void setLastUsedURL(String url) {
		opts.put(OPTION_LASTUSEDURL, url);
		commitPersistentStore();
	}

	public static String getLastUsedURL() {
		boolean b = getBooleanOption(Options.OPTION_REMEMBERURLS, true);
		if (!b)
			return null;
		return getStringOption(Options.OPTION_LASTUSEDURL);
	}

	public static void commitPersistentStore() {
		synchronized (store) {
			store.commit();
		}
	}

	public static boolean getBooleanOption(int option, boolean dflt) {
		return (opts.get(option, dflt));
	}

	public static boolean isOptionSet(int option) {
		return opts.containsKey(option);
	}

	public static int getIntOption(int option, int dflt) {
		return (opts.get(option, dflt));
	}

	public static int getTimeout() {
		int ix = getIntOption(OPTION_TIMEOUT, 0);
		if (ix > timeouts.length)
			ix = 3;
		return (timeouts[ix]);
	}

	public static boolean isReadonly() {
		return getBooleanOption(OPTION_READONLY, false);
	}

	public static String getStringOption(int option) {
		return opts.get(option, null);
	}

	public static String getStringOption(int option, String dflt) {
		return opts.get(option, dflt);
	}

	public static void setOption(int option, String value) {
		opts.put(option, value);
		commitPersistentStore();
	}

	public static void setOption(int option, boolean value) {
		opts.put(option, value);
		commitPersistentStore();
	}

	public static void setOption(int option, int value) {
		opts.put(option, value);
		commitPersistentStore();
	}

	public static class OptionsObject extends SyncItem {

		public boolean setSyncData(DataBuffer dataBuffer, int iv) {
			opts.clear();
			try {
				while (dataBuffer.available() > 0) {
					int t = ConverterUtilities.getType(dataBuffer);
					String s = ConverterUtilities.readString(dataBuffer, true);
					opts.put(t, s);
				}
			} catch (Exception e) {
			}
			commitPersistentStore();
			return (true);
		}

		public boolean getSyncData(DataBuffer dataBuffer, int ix) {
			for (int i = 0; i <= OPTION_LAST; i++) {
				Object o = opts.get(i);
				if (o == null)
					continue;
				ConverterUtilities.writeString(dataBuffer, i, (String) o);
			}
			return (true);
		}

		public int getSyncVersion() {
			return 1;
		}

		public String getSyncName() {
			return ("KeePassBB2 Options1");
		}

		public String getSyncName(Locale locale) {
			return ("KeePassBB2 Options1");
		}
	}

	class BoldLabelField extends LabelField {
		BoldLabelField(String label, long style) {
			super(label, style);
			setFont(getFont().derive(Font.BOLD));
		}
	}
}
