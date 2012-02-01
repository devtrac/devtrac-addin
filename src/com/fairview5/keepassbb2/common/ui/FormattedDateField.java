package com.fairview5.keepassbb2.common.ui;

import java.util.Date;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

public class FormattedDateField extends HorizontalFieldManager {
	static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d yyyy  h:mm aa");
	DateField df;

	public FormattedDateField(String label, String fmt, Date d, long attr) {
		super();
		add(new LabelField(label, DrawStyle.RIGHT));
		long local_attr = Manager.USE_ALL_WIDTH | DrawStyle.RIGHT
				| DateField.DATE_TIME;
		df = new DateField(null, d.getTime(), sdf, local_attr | attr);
		add(df);

	}

	public long getDate() {
		return df.getDate();
	}

	public void setDate(Date d) {
		df.setDate(d);
	}

	public void setDate(long time) {
		df.setDate(time);
	}

}
