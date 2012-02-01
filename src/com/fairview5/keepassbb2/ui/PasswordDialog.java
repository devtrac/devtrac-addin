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
import com.fairview5.keepassbb2.common.ui.PasswordWidget;
import com.fairview5.keepassbb2.common.ui.PathWidget;
import com.fairview5.keepassbb2.kdb4.Kdb4KeyFile;

public class PasswordDialog extends PopupScreen implements FieldChangeListener {
	PasswordWidget efPassword;
	PathWidget pwKeyfile;
	ButtonField bfOk;
	ButtonField bfCancel;
	int rc = Dialog.CANCEL;
	Kdb4KeyFile kf;
	boolean hideKeyfile;
	PasswordDialog me = this;

	public PasswordDialog() {
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR));
		hideKeyfile = Options.getBooleanOption(Options.OPTION_HIDEKEYFILEPROMPT,
				false)
				|| !Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE,
						false);

		String title;
		if (hideKeyfile)
			title = "Enter Master Key";
		else
			title = "Enter Master Key and/or Keyfile";
		add(new LabelField(title));
		add(new SeparatorField());

		efPassword = new PasswordWidget("Key: ");
		String kfpath = null;
		if (Options.getBooleanOption(Options.OPTION_KEYFILERELOAD, false)) {
			kfpath = Options.getStringOption(Options.OPTION_DEFAULTKEYFILEURL,
					null);
		}

		pwKeyfile = new PathWidget("File: ", kfpath, false);

		add(efPassword);
		if (!hideKeyfile) {
			add(new SeparatorField());
			add(pwKeyfile);
		}

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
		} else if (c == Characters.ENTER) {
			if (efPassword.isFocus()) {
				fieldChanged(bfOk, FieldChangeListener.PROGRAMMATIC);
				return true;
			}
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
		return efPassword.getText();
	}

	public String getKeyFilePath() {
		return pwKeyfile.getText();
	}

	public Kdb4KeyFile getKeyFile() {
		return kf;
	}

	public void fieldChanged(Field field, int i) {
		if (field.equals(bfOk)) {
			if (efPassword.getTextLength() == 0 && pwKeyfile.getTextLength() == 0) {
				Dialog.alert("Master key and keyfile can't both be empty");
				return;
			}
			if (pwKeyfile.getTextLength() > 0) {
				KFile f = null;
				try {
					f = new KFile(pwKeyfile.getText());
					if (f.exists()) {
						kf = new Kdb4KeyFile(f.readContents());
					}
				} catch (Exception e) {
					Dialog.alert(e.getMessage());
					rc = Dialog.CANCEL;
					close();
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
