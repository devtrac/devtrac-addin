package com.fairview5.keepassbb2.common.ui;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class PasswordWidget extends VerticalFieldManager implements
		FieldChangeListener {

	PasswordEditField pef;
	EditField bef;
	BasicEditField cef;
	CheckboxField cf;
	String label;

	public PasswordWidget(String label) {
		super(NO_VERTICAL_SCROLL | NO_HORIZONTAL_SCROLL);
		this.label = label;
		pef = new PasswordEditField(label, null);
		bef = new EditField(label, null, TextField.DEFAULT_MAXCHARS,
				TextField.NO_LEARNING | TextField.NO_NEWLINE);
		cf = new CheckboxField("Show characters", false);
		cf.setChangeListener(this);
		add(pef);
		add(cf);
		cef = pef;
	}

	public void fieldChanged(Field field, int context) {
		if (field != cf)
			return;
		if (cf.getChecked()) {
			bef.setText(pef.getText());
			replace(pef, bef);
			cef = bef;
		} else {
			pef.setText(bef.getText());
			replace(bef, pef);
			cef = pef;
		}
	}

	public String getText() {
		return cef.getText();
	}

	public int getTextLength() {
		return cef.getTextLength();
	}

}
