/*
KeePass for BlackBerry
Copyright 2007,2008 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

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

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

import com.fairview5.keepassbb2.Options;
import com.fairview5.keepassbb2.common.file.KFile;
import com.fairview5.keepassbb2.common.ui.PathWidget;
import com.fairview5.keepassbb2.kdb4.Kdb4KeyFile;

public class DatabasePropertiesDialog extends PopupScreen implements
		FieldChangeListener {
	PasswordEditField ef1;
	PasswordEditField ef2;
	EditField ef3;
	PathWidget path;
	ButtonField bfOk;
	ButtonField bfCancel;
	int rc = Dialog.CANCEL;
	Kdb4KeyFile kf = null;

	public DatabasePropertiesDialog(int ncr) {
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR));
		ef1 = new PasswordEditField("Master Key:", null);
		ef2 = new PasswordEditField("Retype Key:", null);
		path = new PathWidget("Keyfile:", null, true);
		ef3 = new EditField("Key Encryption Rounds:", ncr == 0 ? "6000"
				: ncr + "", 10, EditField.FILTER_NUMERIC);

		add(new LabelField("Enter Keys and/or Keyfile:"));
		add(new SeparatorField());
		add(ef1);
		add(ef2);
		if (Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE, true)) {
			add(new SeparatorField());
			add(path);
		}
		add(new SeparatorField());
		add(ef3);

		add(new SeparatorField());
		HorizontalFieldManager hfm = new HorizontalFieldManager();
		add(hfm);
		bfOk = new ButtonField("Ok");
		bfOk.setChangeListener(this);
		bfCancel = new ButtonField("Cancel");
		bfCancel.setChangeListener(this);
		hfm.add(bfOk);
		hfm.add(bfCancel);

	}

	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			fieldChanged(bfCancel, FieldChangeListener.PROGRAMMATIC);
			return true;
		}
		return super.keyChar(c, status, time);
	}

	public int doModal() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return (rc);
	}

	public int getRC() {
		return (rc);
	}

	public String getPassword() {
		return ef1.getText();
	}

	public String getKeyFilePath() {
		return path.getText();
	}

	public Kdb4KeyFile getKeyFile() {
		return kf;
	}

	public int getKeyRounds() {
		return Integer.parseInt(ef3.getText());
	}

	public void fieldChanged(Field field, int i) {
		if (field.equals(bfOk)) {
			if (!ef1.getText().equals(ef2.getText())) {
				Dialog.alert("Master keys do not match.");
				return;
			}
			if (ef1.getTextLength() == 0 && path.getTextLength() == 0) {
				Dialog.alert("Master keys and keyfile can't both be empty.");
				return;
			}

			if (path.getTextLength() > 0) {
				KFile f = null;
				try {
					f = new KFile(path.getText());
					if (f.exists()) {
						kf = new Kdb4KeyFile(f.readContents());
					} else {
						kf = new Kdb4KeyFile();
						kf.generateNew();
						int drc = Dialog.ask(Dialog.D_YES_NO, "Do you want to protect this keyfile so that ONLY THIS DEVICE can read it?");
						if (drc == Dialog.YES) {
							f.enableDRMForwardLock();
							f.setControlledAccess();
						}
						f.create();
						f.write(kf.exportKeyText().getBytes());
						Dialog.alert("Keyfile written to: "
								+ f.getFullName());
					}
				} catch (Exception e) {
					Dialog.alert(e.getMessage());
					return;
				} finally {
					if (f != null)
						f.close();
				}
			}
			rc = Dialog.OK;
			close();
		} else if (field.equals(bfCancel)) {
			rc = Dialog.CANCEL;
			close();
		}
	}
}
