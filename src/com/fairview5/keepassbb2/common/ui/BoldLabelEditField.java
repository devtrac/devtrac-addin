package com.fairview5.keepassbb2.common.ui;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.HorizontalFieldManager;

public class BoldLabelEditField extends HorizontalFieldManager {

	public static final int TYPE_EDITFIELD = 0;
	//public static final int TYPE_TEXTFIELD = 1;
	public static final int TYPE_ACTIVESINGLELINE = 2;
	public static final int TYPE_PASSWORD = 3;
	public static final int TYPE_ACTIVEMULTILINE = 4;
	public static final int TYPE_READONLY = 5;

	private TextField fr = null;

	public BoldLabelEditField(String fleft, String fright, int type) {

		LabelField lf = new LabelField(fleft);
		Font lfont = lf.getFont();
		Font nf = lfont.derive(Font.BOLD);
		lf.setFont(nf);
		add(lf);

		switch (type) {
		case TYPE_EDITFIELD:
			fr = new EditField(null, fright, TextField.DEFAULT_MAXCHARS,
					EditField.NO_NEWLINE);
			break;
		case TYPE_PASSWORD:
			fr = new EditField(null, fright, TextField.DEFAULT_MAXCHARS,
					EditField.NO_NEWLINE | TextField.NO_COMPLEX_INPUT
							| TextField.NO_LEARNING | Field.NON_SPELLCHECKABLE);
			break;
//		case TYPE_TEXTFIELD:
//			fr = new TextField(null, fright, TextField.DEFAULT_MAXCHARS,
//					EditField.NO_NEWLINE);
//			break;
		case TYPE_ACTIVESINGLELINE:
			fr = new ActiveAutoTextEditField(null, fright,
					TextField.DEFAULT_MAXCHARS, EditField.NO_NEWLINE);
			break;
		case TYPE_ACTIVEMULTILINE:
			fr = new ActiveAutoTextEditField(null, fright,
					TextField.DEFAULT_MAXCHARS, 0);
			break;
		case TYPE_READONLY:
			fr = new EditField(null, fright, TextField.DEFAULT_MAXCHARS,
					EditField.NO_NEWLINE | Field.READONLY | Field.NON_FOCUSABLE);
			break;

		}

		add(fr);
	}

	public String getText() {
		return fr.getText();
	}

	public void setLeafCookie(Object cookie) {
		fr.setCookie(cookie);
	}

}
