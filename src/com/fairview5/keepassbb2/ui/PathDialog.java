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
package com.fairview5.keepassbb2.ui;

import java.io.IOException;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

import com.fairview5.keepassbb2.common.file.KFile;
import com.fairview5.keepassbb2.common.ui.PathWidget;

public final class PathDialog extends PopupScreen implements
		FieldChangeListener {

	PathWidget pwURL;
	ButtonField bfOk;
	ButtonField bfCancel;
	int rc;
	boolean saveAs;
	CheckboxField cbSecureMode;
	CheckboxField cbSetDefault;

	public PathDialog(String label, boolean saveAs) {
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR));
		this.saveAs = saveAs;
		add(new LabelField(label));
		add(new SeparatorField());

		pwURL = new PathWidget(null, null, saveAs);
		add(pwURL);
		add(new SeparatorField());

		cbSecureMode = new CheckboxField("Only THIS DEVICE can read this file",
				false);
		cbSetDefault = new CheckboxField("Set as default database", false);
		if (saveAs) {
			// add(cbSecureMode);
			// add(cbSetDefault);
			add(new SeparatorField());
		}

		HorizontalFieldManager hfm = new HorizontalFieldManager();
		add(hfm);
		bfOk = new ButtonField("Ok");
		bfOk.setChangeListener(this);
		bfCancel = new ButtonField("Cancel");
		bfCancel.setChangeListener(this);
		hfm.add(bfOk);
		hfm.add(bfCancel);
	}

	public int doModal() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return (rc);
	}

	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			fieldChanged(bfCancel, FieldChangeListener.PROGRAMMATIC);
			return true;
		} else if (c == Characters.ENTER) {
			if (pwURL.url.isFocus()) {
				fieldChanged(bfOk, FieldChangeListener.PROGRAMMATIC);
				return true;
			}
		}
		return super.keyChar(c, status, time);
	}

	public void fieldChanged(Field field, int i) {
		if (field.equals(bfOk)) {
			String url = pwURL.getText();
			if (url == null || url.length() == 0) {
				Dialog.alert("Please specify a URL");
				return;
			}
			rc = Dialog.OK;
		} else if (field.equals(bfCancel)) {
			rc = Dialog.CANCEL;
		}
		close();
	}

	public String getText() {
		return (pwURL.getText());
	}

	public KFile getSelectedFileObject() throws IOException {
		return (pwURL.getSelectedFileObject());
	}

	public boolean getSecureMode() {
		return (cbSecureMode.getChecked());
	}

	public boolean getSetDefault() {
		return (cbSetDefault.getChecked());
	}

}
