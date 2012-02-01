package com.fairview5.keepassbb2.common.util;

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.synchronization.SyncItem;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.IntHashtable;

import com.fairview5.keepassbb2.common.ui.BooleanChoiceField;

public abstract class CommonOptions extends MainScreen {

	public static final int OPTION_FONT_FAMILY = -1;
	public static final int OPTION_FONT_SIZE = -2;
	public static final int OPTION_FIRST = -2;
	public static int OPTION_LAST = -1;
	public static String optionSyncName;

	static PersistentObject store;
	static TypedIntHashtable opts;
	static IntHashtable registry = new IntHashtable();

	public static void registerField(int option, Field f) {
		registry.put(option, f);
	}

	public static Field getRegisteredField(int option) {
		return (Field) registry.get(option);
	}

	public static String getOptionSyncName() {
		return optionSyncName;
	}

	public static void setOptionSyncName(String option_sync_name) {
		optionSyncName = option_sync_name;
	}



	public static Font getMenuActionFont() {
		Font f = getDefaultFont();
		int fh = f.getHeight();
		Font nf = f.derive(Font.PLAIN, fh - 1);
		return nf;
	}

	public static Font getMenuHeaderFont() {
		Font f = getDefaultFont();
		int fh = f.getHeight();
		Font nf = f.derive(Font.ITALIC, fh - 2);
		return nf;
	}

	public static Font getDefaultFont() {
		if (!isOptionSet(OPTION_FONT_FAMILY)) {
			return Font.getDefault();
		}
		FontFamily dff = Font.getDefault().getFontFamily();
		String ffam = getStringOption(OPTION_FONT_FAMILY, dff.getName());
		FontFamily fam = null;
		try {
			fam = FontFamily.forName(ffam);
		} catch (ClassNotFoundException e) {
		}

		int dfh = Ui.convertSize(Font.getDefault().getHeight(), Ui.UNITS_px,
				Ui.UNITS_pt);
		int fpt = getIntOption(OPTION_FONT_SIZE, dfh);

		Font f = fam.getFont(Font.PLAIN, fpt, Ui.UNITS_pt);
		return (f);
	}

	public BooleanChoiceField createBooleanChoiceField(int option, boolean def,
			String label, boolean visible) {
		boolean checked = getBooleanOption(option, def);
		BooleanChoiceField bcf = new BooleanChoiceField(label, checked);
		return bcf;
	}

	public BooleanChoiceField createBooleanChoiceField(int option, boolean def,
			String label) {
		return createBooleanChoiceField(option, def, label, true);
	}

	public ObjectChoiceField createStringChoiceField(int option, String def,
			String label, String[] objects) {
		String s = getStringOption(option, def);
		int ix = 0;
		if (s != null) {
			for (int i = 0; i < objects.length; i++) {
				if (s.equals(objects[i]))
					ix = i;
			}
		}
		return new ObjectChoiceField(label, objects, ix);
	}

	public ObjectChoiceField createIntChoiceField(int option, int def,
			String label, int[] objects) {
		int s = getIntOption(option, def);
		int ix = 0;
		Integer[] ia = new Integer[objects.length];
			for (int i = 0; i < objects.length; i++) {
				ia[i] = new Integer(objects[i]);
				if (s == objects[i])
					ix = i;
			}
		return new ObjectChoiceField(label, ia, ix);
	}
	
	public static void commitPersistentStore() {
		synchronized (store) {
			store.commit();
		}
	}

	public static boolean getBooleanOption(int option, boolean dflt) {
		return (opts.get(option, dflt));
	}

	public static boolean isOptionSet(int option) {
		return opts.containsKey(option);
	}

	public static int getIntOption(int option, int dflt) {
		return (opts.get(option, dflt));
	}

	public static String getStringOption(int option) {
		return opts.get(option, null);
	}

	public static String getStringOption(int option, String dflt) {
		return opts.get(option, dflt);
	}

	public static void setOption(int option, String value) {
		opts.put(option, value);
		commitPersistentStore();
	}

	public static void setOption(int option, boolean value) {
		opts.put(option, value);
		commitPersistentStore();
	}

	public static void setOption(int option, int value) {
		opts.put(option, value);
		commitPersistentStore();
	}

	public static class OptionsObject extends SyncItem {

		public boolean setSyncData(DataBuffer dataBuffer, int iv) {
			opts.clear();
			try {
				while (dataBuffer.available() > 0) {
					int t = ConverterUtilities.getType(dataBuffer);
					String s = ConverterUtilities.readString(dataBuffer, true);
					opts.put(t, s);
				}
			} catch (Exception e) {
			}
			commitPersistentStore();
			return (true);
		}

		public boolean getSyncData(DataBuffer dataBuffer, int ix) {
			for (int i = 0; i <= OPTION_LAST; i++) {
				Object o = opts.get(i);
				if (o == null)
					continue;
				ConverterUtilities.writeString(dataBuffer, i, (String) o);
			}
			return (true);
		}

		public int getSyncVersion() {
			return 1;
		}

		public String getSyncName() {
			return (optionSyncName);
		}

		public String getSyncName(Locale locale) {
			return (optionSyncName);
		}
	}

}
