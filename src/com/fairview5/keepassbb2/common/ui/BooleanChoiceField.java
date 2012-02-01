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

import net.rim.device.api.ui.component.ObjectChoiceField;

public class BooleanChoiceField extends ObjectChoiceField {

        static String[][] choices = {{"Yes", "No"}, {"True", "False"}, {"On", "Off"}};
        public static final int STYLE_YESNO = 0;
        public static final int STYLE_TRUEFALSE = 1;
        public static final int STYLE_ONOFF = 2;
        private int style = STYLE_YESNO;

        public BooleanChoiceField(String label, boolean initialValue, long style) {
                super(label, choices[STYLE_ONOFF], initialValue ? 0 : 1, style);
        }

        public BooleanChoiceField(String label, boolean initialValue) {
                super(label, choices[STYLE_ONOFF], initialValue ? 0 : 1, 0);
        }

        public BooleanChoiceField(String label, int style, boolean initialValue) {
                super(label, choices[style], initialValue ? 0 : 1, 0);
                this.style = style;
        }

        public BooleanChoiceField(String label, String trueValue, String falseValue, boolean initialValue) {
                super(label, new String[] { trueValue, falseValue }, initialValue ? 0 : 1, 0);
        }

        public boolean getChecked() {
                return getSelectedIndex() == 0 ? true : false;
        }

        public String getCheckedString() {
           return choices[style][getSelectedIndex()];
        }
        
        public void setChecked(boolean b) {
                this.setSelectedIndex(b ? 0 : 1);
        }
}
