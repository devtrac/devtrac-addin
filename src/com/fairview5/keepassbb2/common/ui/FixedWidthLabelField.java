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

package com.fairview5.keepassbb2.common.ui;

import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.LabelField;

public class FixedWidthLabelField extends LabelField {
	private Font defaultFont;
	private String spacer;

	public FixedWidthLabelField(String text, String spacer, long style) {
		super(text, style);
		this.spacer = spacer;
		// Font f = getFont();
		// setFont(f.derive(Font.BOLD));
	}

	public int getPreferredWidth() {
		defaultFont = Font.getDefault();
		return defaultFont.getAdvance(spacer);
	}

	protected void layout(int width, int height) {
		width = getPreferredWidth();
		height = super.getPreferredHeight();
		super.layout(width, height);
		super.setExtent(width, height);
	}
}
