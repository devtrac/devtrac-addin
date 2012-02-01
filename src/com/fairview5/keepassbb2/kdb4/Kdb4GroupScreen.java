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

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.fairview5.keepassbb2.Options;
import com.fairview5.keepassbb2.common.ui.BoldLabelEditField;
import com.fairview5.keepassbb2.common.ui.FixedWidthLabelField;
import com.fairview5.keepassbb2.ui.*;

public class Kdb4GroupScreen extends MainScreen {
	static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d yyyy  h:mm aa");

	Kdb4GroupScreen me;
	final Kdb4Group pwg;

	BoldLabelEditField efTitle;

	DateField dfCreated;
	DateField dfExpires;
	DateField dfLastAccess;
	DateField dfLastMod;

	IconChooserWidget icwIcon;

	int rc = Dialog.CANCEL;

	public Kdb4GroupScreen(final Kdb4Group e) {
		me = this;
		this.pwg = e;

		if (Options.getBooleanOption(Options.OPTION_HIDETITLE, false))
			setTitle((Field) null);
		else
			setTitle(pwg.getStringDataElement("Name"));

		long attr = 0;

		attr = TextField.EDITABLE;

		efTitle = new BoldLabelEditField("Name: ", pwg
				.getStringDataElement("Name"), BoldLabelEditField.TYPE_EDITFIELD);

		add(efTitle);

		add(new SeparatorField());
		icwIcon = new IconChooserWidget(TreeScreen.mIcon, pwg
				.getIntDataElement("IconID"));
		add(icwIcon);
		add(new SeparatorField());
		attr = 0;
		HorizontalFieldManager h1 = new HorizontalFieldManager(attr);
		HorizontalFieldManager h2 = new HorizontalFieldManager(attr);
		HorizontalFieldManager h3 = new HorizontalFieldManager(attr);
		HorizontalFieldManager h4 = new HorizontalFieldManager(attr);

		attr = DrawStyle.RIGHT;
		h1.add(new FixedWidthLabelField("Created:", "Last Mod:", attr));
		h2.add(new FixedWidthLabelField("Expires:", "Last Mod:", attr));
		h3.add(new FixedWidthLabelField("Last Acc:", "Last Mod:", attr));
		h4.add(new FixedWidthLabelField("Last Mod:", "Last Mod:", attr));

		attr = Manager.USE_ALL_WIDTH | DrawStyle.RIGHT | DateField.DATE_TIME;
		dfCreated = new DateField(null, pwg.getDate("CreationTime").getTime(),
				sdf, attr | Field.READONLY);
		dfExpires = new DateField(null, pwg.getDate("ExpiryTime").getTime(), sdf,
				attr);
		dfLastAccess = new DateField(null, pwg.getDate("LastAccessTime")
				.getTime(), sdf, attr | Field.READONLY);
		dfLastMod = new DateField(null, pwg.getDate("LastModificationTime")
				.getTime(), sdf, attr | Field.READONLY);
		h1.add(dfCreated);
		h2.add(dfExpires);
		h3.add(dfLastAccess);
		h4.add(dfLastMod);

		add(h1);
		add(h2);
		add(h3);
		add(h4);

		pwg.setDate("LastAccessTime", new Date());
		if (Options.getBooleanOption(Options.OPTION_SAVELASTACCESS, false))
			setDirty(true);
	}

	public void makeMenu(Menu km, int instance) {
		if (isDirty()) {
			km.add(new MenuItem("Discard Changes", 100, 0) {
				public void run() {
					me.setDirty(false);
					close();
				}
			});
		}
		if (isDirty()) {
			km.add(new MenuItem("Save", 110, 0) {
				public void run() {
					me.setDirty(false);
					onSave();
				}
			});
		}
		km.add(new MenuItem("Close", 120, 0) {
			public void run() {
				close();
			}
		});

	}

	public boolean isDataValid() {
		if (efTitle.getText().length() <= 0) {
			Dialog.alert("Name can't be empty");
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
		pwg.setDataElement("Name", efTitle.getText());
		pwg.setDate("LastModifiedTime", new Date());
		pwg.setDate("ExpiryTime", new Date(dfExpires.getDate()));
		pwg.setDataElement("IconID", icwIcon.getSelectedIcon());
		rc = Dialog.OK;
		pwg.setDirty();
	}

	public void show() {
		UiApplication.getUiApplication().pushScreen(this);
	}

	public int doModal() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return (rc);
	}

}
