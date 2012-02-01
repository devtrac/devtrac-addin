package com.fairview5.keepassbb2.common.ui;

import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.LabelField;

public class BoldFieldLabel extends LabelField {
	class BoldLabelField extends LabelField {
		BoldLabelField(String label, long style) {
			super(label, style);
			setFont(getFont().derive(Font.BOLD));
		}
	}
}
