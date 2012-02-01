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

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

public class JustifiedEditField extends HorizontalFieldManager implements FieldChangeListener {

	LabelField lf;
	EditField ef;
	int dw = Display.getWidth();
	int rightMargin;

	public JustifiedEditField(String label, String value, int maxChars, long style) {
		super(USE_ALL_WIDTH);
		ef = new EditField(null, value, maxChars, style) {
			protected void onDisplay() {
				update(0);
			}

			protected void update(int d) {
				super.update(d);
				int efw = getFont().getAdvance(super.getText());
				setPosition(dw - efw - rightMargin, 0);
			}
		};
		ef.setChangeListener(this);
		lf = new LabelField(label);
		add(lf);
		add(ef);
	}

	public LabelField getLabelField() {
		return lf;
	}

	public EditField getEditField() {
		return ef;
	}

	public String getText() {
		return ef.getText();
	}

	public void setRightMargin(int m) {
		rightMargin = m;
	}

	public int getRightMargin() {
		return rightMargin;
	}

	public void fieldChanged(Field field, int context) {
		invalidate();
	}

}
