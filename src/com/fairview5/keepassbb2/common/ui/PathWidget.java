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
package com.fairview5.keepassbb2.common.ui;

import java.io.IOException;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.fairview5.keepassbb2.common.file.FileExplorerScreen;
import com.fairview5.keepassbb2.common.file.KFile;

public class PathWidget extends HorizontalFieldManager implements
		FieldChangeListener {

	public EditField url;
	ButtonField button;
	boolean saveAs;
	FileExplorerScreen fes;
	int fesrc = Dialog.CANCEL;
	int rc = Dialog.CANCEL;

	public PathWidget(String label, String initialValue) {
		this(label, initialValue, false);
	}

	public PathWidget(String label, String initialValue, boolean saveAs) {
		super();
		this.saveAs = saveAs;
		url = new EditField(label, initialValue, 256, Field.USE_ALL_WIDTH | Field.FIELD_VCENTER) {
			public boolean keyChar(char c, int status, int time) {
				if (c == Characters.ENTER) {
					return true;
				}
				return super.keyChar(c, status, time);
			}
		};

		button = new ButtonField("...") {
			public int getPreferredHeight() {
				return 12;
			}

			public int getPreferredWidth() {
				return 16;
			}
		};

		button.setChangeListener(this);
		add(button);
		add(url);
	}

	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ENTER) {
			if (button.isFocus()) {
				fieldChanged(button, FieldChangeListener.PROGRAMMATIC);
				return true;
			}
			return false;
		}
		return super.keyChar(c, status, time);
	}

	public void fieldChanged(Field field, int context) {
		if (field.equals(button)) {
			try {
				fes = new FileExplorerScreen(saveAs, "/");
			} catch (IOException e) {
				Dialog.alert("Unable to initilialize the File Explorer: "
						+ e.getMessage());
				return;
			}

			int rc = fes.doModal();
			if (rc != Dialog.OK)
				return;
			String u = fes.getSelectedFileName();
			if (u != null) {
				url.setFocus();
				if (!u.equalsIgnoreCase(url.getText())) {
					url.setText(u);
					url.setDirty(true);
				}
			}

		}
	}

	public String getText() {
		return url.getText();
	}

	public boolean isDirty() {
		return url.isDirty();
	}

	public KFile getSelectedFileObject() throws IOException {
		if (fes != null && fesrc == Dialog.OK)
			return fes.getSelectedFileObject();
		return new KFile(getText());
	}

	public int getTextLength() {
		return url.getTextLength();
	}
}
