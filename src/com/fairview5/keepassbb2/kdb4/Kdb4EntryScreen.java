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

import java.util.Date;
import java.util.Vector;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;

import com.fairview5.keepassbb2.Options;
import com.fairview5.keepassbb2.common.ui.*;
import com.fairview5.keepassbb2.ui.IconChooserWidget;
import com.fairview5.keepassbb2.ui.TreeScreen;

public class Kdb4EntryScreen extends MainScreen {
	static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d yyyy  h:mm aa");

	Kdb4EntryScreen me;
	final Kdb4Entry pwe;

	BoldLabelEditField efTitle;
	BoldLabelEditField efParentGroup;
	BoldLabelEditField efURL;
	BoldLabelEditField efUsername;
	BoldLabelEditField efPassword;
	BoldLabelEditField efNotes;

	FormattedDateField dfCreated;
	FormattedDateField dfExpires;
	FormattedDateField dfLastAccess;
	FormattedDateField dfLastMod;
	BooleanChoiceField cbExpires;

	IconChooserWidget icwIcon;

	Vector udfs = new Vector();

	int rc = Dialog.CANCEL;

	public Kdb4EntryScreen(final Kdb4Entry e) {
		me = this;
		this.pwe = e;

		if (Options.getBooleanOption(Options.OPTION_HIDETITLE, false))
			setTitle((Field) null);
		else
			setTitle(pwe.getStringDynamicAttribute("Title"));

		long attr = 0;

		attr = TextField.EDITABLE;

		efTitle = new BoldLabelEditField("Title: ", pwe
				.getStringDynamicAttribute("Title"),
				BoldLabelEditField.TYPE_EDITFIELD);

		efURL = new BoldLabelEditField("URL: ", pwe
				.getStringDynamicAttribute("URL"),
				BoldLabelEditField.TYPE_ACTIVESINGLELINE);

		efUsername = new BoldLabelEditField("User: ", pwe
				.getStringDynamicAttribute("UserName"),
				BoldLabelEditField.TYPE_EDITFIELD);

		efPassword = new BoldLabelEditField("Pass: ", pwe
				.getStringDynamicAttribute("Password"),
				BoldLabelEditField.TYPE_PASSWORD);

		efNotes = new BoldLabelEditField("Notes: ", pwe
				.getStringDynamicAttribute("Notes"),
				BoldLabelEditField.TYPE_ACTIVEMULTILINE);

		if (Options.getBooleanOption(Options.OPTION_SHOW_GROUP, false)
				&& pwe.parent != null) {
			efParentGroup = new BoldLabelEditField("Group: ", pwe.parent
					.toString(), BoldLabelEditField.TYPE_READONLY);
		}

		int cs = pwe.childElements.size();
		for (int i = 0; i < cs; i++) {
			Kdb4Object o = pwe.childElements.objectAt(i);
			if (!(o instanceof Kdb4Entry.StringAttribute))
				continue;
			Kdb4Entry.StringAttribute sa = (Kdb4Entry.StringAttribute) o;
			String key = sa.getKey();
			if (key.equals("Title") || key.equals("URL") || key.equals("UserName")
					|| key.equals("Password") || key.equals("Notes"))
				continue;

			BoldLabelEditField ef = new BoldLabelEditField(key + ": ", sa
					.getValue(), BoldLabelEditField.TYPE_ACTIVESINGLELINE);

			ef.setCookie("UDF:" + key);

			udfs.addElement(ef);
		}
		
		icwIcon = new IconChooserWidget(TreeScreen.mIcon, pwe
				.getIntDataElement("IconID"));

		dfCreated = new FormattedDateField("Created:", "Last Mod:", pwe
				.getDate("CreationTime"), Field.READONLY);

		dfExpires = new FormattedDateField("Expires:", "Last Mod:", pwe
				.getDate("ExpiryTime"), 0);

		cbExpires = new BooleanChoiceField("Expires?", 0, pwe.getExpires());

		dfLastAccess = new FormattedDateField("Last Acc:", "Last Mod:", pwe
				.getDate("LastAccessTime"), Field.READONLY);

		dfLastMod = new FormattedDateField("Last Mod:", "Last Mod:", pwe
				.getDate("LastModificationTime"), Field.READONLY);

		addFields(pwe.parent != null);

		pwe.setDate("LastAccessTime", new Date());
		if (Options.getBooleanOption(Options.OPTION_SAVELASTACCESS, false))
			setDirty(true);
	}

	private void addFields(boolean hideEmpty) {

		boolean hideEmptyOpt = Options.getBooleanOption(
				Options.OPTION_HIDE_EMPTY_FIELDS, false)
				& hideEmpty;

		if (!(hideEmptyOpt && efTitle.getText().length() == 0)) {
			add(efTitle);
		}

		if (Options.getBooleanOption(Options.OPTION_SHOW_GROUP, false)
				&& pwe.parent != null) {
			add(new SeparatorField());
			add(efParentGroup);
		}

		if (!(hideEmptyOpt && efURL.getText().length() == 0)) {
			add(new SeparatorField());
			add(efURL);
		}

		if (!(hideEmptyOpt && efUsername.getText().length() == 0)) {
			add(new SeparatorField());
			add(efUsername);
		}

		if (!(hideEmptyOpt && efPassword.getText().length() == 0)) {
			add(new SeparatorField());
			add(efPassword);
		}

		int udfCount = udfs.size();
		for (int i = 0; i < udfCount; i++) {
			add(new SeparatorField());
			add((Field) udfs.elementAt(i));
		}

		NullField nf = new NullField(Field.NON_FOCUSABLE);
		nf.setCookie("NF");
		add(nf);

		if (!(hideEmptyOpt && efNotes.getText().length() == 0)) {
			add(new SeparatorField());
			add(efNotes);
		}

		add(new SeparatorField());
		add(icwIcon);

		add(new SeparatorField());
		add(dfCreated);
		add(new SeparatorField());
		add(dfExpires);
		add(cbExpires);
		add(new SeparatorField());
		add(dfLastAccess);
		add(new SeparatorField());
		add(dfLastMod);

	}

	public void makeMenu(Menu km, int instance) {

		km.add(new MenuItem("Show hidden fields", 5, 0) {
			public void run() {
				deleteAll();
				addFields(false);
			}
		});
		km.add(new MenuItem("Hide empty fields", 6, 0) {
			public void run() {
				deleteAll();
				addFields(true);
			}
		});

		km.add(new MenuItem("Add user defined field", 10, 0) {
			public void run() {

				EditField efKey = new EditField("Key: ", null,
						TextField.DEFAULT_MAXCHARS, EditField.NO_NEWLINE);
				EditField efValue = new EditField("Value: ", null,
						TextField.DEFAULT_MAXCHARS, EditField.NO_NEWLINE);

				EditDialog ed = new EditDialog("Enter user defined attribute:");
				ed.addField(efKey);
				ed.addField(efValue);
				int rc = ed.doModal();
				if (rc != Dialog.OK)
					return;
				String key = efKey.getText();
				String value = efValue.getText();
				BoldLabelEditField ef = new BoldLabelEditField(key + ": ", value,
						BoldLabelEditField.TYPE_ACTIVESINGLELINE);
				ef.setCookie("UDF:" + key);

				int nfc = 0;
				int fc = getFieldCount();
				for (int i = 0; i < fc; i++) {
					Field f = getField(i);
					if (f instanceof NullField) {
						nfc = i;
						break;
					}
				}
				insert(new SeparatorField(), nfc);
				insert(ef, nfc + 1);
				setDirty(true);
			}
		});

		final Object cook = getFieldWithFocus().getCookie();

		if (cook instanceof String && ((String) cook).startsWith("UDF:")) {
			km.add(new MenuItem("Delete user defined field", 20, 0) {
				public void run() {

					if (Options.getBooleanOption(Options.OPTION_DELETEPROMPT, true)) {
						int rc = Dialog
								.ask(Dialog.D_DELETE,
										"Are you sure you want to delete this user defined field?");
						if (rc != Dialog.DELETE)
							return;
					}

					int six = getFieldWithFocusIndex();
					delete(getField(six - 1));
					Field f = getFieldWithFocus();
					delete(f);
					pwe.deleteStringAttribute(((String) cook).substring(4));
				}
			});
		}
		km.addSeparator();

		if (isDirty()) {
			km.add(new MenuItem("Discard Changes", 100, 0) {
				public void run() {
					me.setDirty(false);
					close();
				}
			});
			km.add(new MenuItem("Save", 120, 0) {
				public void run() {
					me.setDirty(false);
					onSave();
				}
			});
		}

		km.add(new MenuItem("Close", 130, 0) {
			public void run() {
				close();
			}
		});

	}

	public boolean isDataValid() {
		if (efTitle.getText().length() <= 0) {
			Dialog.alert("Title can't be empty");
			return (false);
		}
		return (true);
	}

	protected boolean onSavePrompt() {

		if (Options.getBooleanOption(Options.OPTION_AUTOACCEPT, false)) {
			return onSave();
		}
		if (!super.isDirty()
				&& Options.getBooleanOption(Options.OPTION_SAVELASTACCESS, false))
			return onSave();

		return super.onSavePrompt();
	}

	public void save() {
		pwe.setDynamicAttribute("Title", efTitle.getText());
		pwe.setDynamicAttribute("URL", efURL.getText());
		pwe.setDynamicAttribute("UserName", efUsername.getText());
		pwe.setDynamicAttribute("Password", efPassword.getText());
		pwe.setDynamicAttribute("Notes", efNotes.getText());
		pwe.setDate("LastModificationTime", new Date());
		pwe.setExpired(cbExpires.getChecked(), new Date(dfExpires.getDate()));
		pwe.setDataElement("IconID", icwIcon.getSelectedIcon());

		int fc = getFieldCount();
		for (int i = 0; i < fc; i++) {
			Field f = getField(i);
			if (f instanceof BoldLabelEditField && f.getCookie() instanceof String
					&& ((String) f.getCookie()).startsWith("UDF:")) {
				pwe.setDynamicAttribute(((String) f.getCookie()).substring(4),
						((BoldLabelEditField) f).getText());
			}
		}

		rc = Dialog.OK;
		pwe.parsingFinished();
		pwe.setDirty();
	}

	public void show() {
		UiApplication.getUiApplication().pushScreen(this);
	}

	public int doModal() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return (rc);
	}

}
