package com.fairview5.keepassbb2.common.ui;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.SimpleSortingIntVector;

public class FontSelectionWidget extends VerticalFieldManager implements FieldChangeListener, FocusChangeListener{

	ObjectChoiceField ofFontFamily;
	ObjectChoiceField ofFontSize;
	LabelField lfFontSample;
	
	public FontSelectionWidget(String fontFamily, int fontPointSize) {
		super(Manager.USE_ALL_WIDTH);
		
		ofFontFamily = createFontFamilyField(fontFamily);
		ofFontSize = createFontSizeField(fontFamily, fontPointSize);
		lfFontSample = new LabelField("The quick brown fox jumps over the lazy dog's back.");
		fieldChanged(ofFontFamily, 0);
		add(ofFontFamily);
		add(ofFontSize);
		add(lfFontSample);
	}
	
	public ObjectChoiceField createFontFamilyField(String defaultFamilyName) {
		FontFamily[] ffa = FontFamily.getFontFamilies();
		FontFamily dff = Font.getDefault().getFontFamily();
		String ffam = (defaultFamilyName == null ? dff.getName() : defaultFamilyName);
		String[] famnames = new String[ffa.length];
		int ffix = 0;
		for (int i = 0; i < ffa.length; i++) {
			if (ffa[i].getName().equals(ffam))
				ffix = i;
			famnames[i] = ffa[i].getName();
		}
		return new ObjectChoiceField("Base Font Family", famnames, ffix);
	}

	public ObjectChoiceField createFontSizeField(String ffam, int fontSize) {
		FontFamily[] ffa = FontFamily.getFontFamilies();
		String[] famnames = new String[ffa.length];
		int ffix = 0;
		for (int i = 0; i < ffa.length; i++) {
			if (ffa[i].getName().equals(ffam))
				ffix = i;
			famnames[i] = ffa[i].getName();
		}

		int[] fsa = ffa[ffix].getHeights();
		int fhix = 0;
		SimpleSortingIntVector v = new SimpleSortingIntVector();
		for (int i = 0; i < fsa.length; i++) {
			int fz = Ui.convertSize(fsa[i], Ui.UNITS_px, Ui.UNITS_pt);
			if (!v.contains(fz)) {
				v.addElement(fz);
				if (fz == fontSize)
					fhix = v.indexOf(fz);
			}
		}
		int[] via = v.getArray();
		Integer[] ia = new Integer[via.length];
		for (int i = 0; i < ia.length; i++)
			ia[i] = new Integer(via[i]);

		return new ObjectChoiceField("Base Font Size", ia, fhix);

	}
	
	public void focusChanged(Field field, int eventType) {
		if (field == ofFontFamily || field == ofFontSize) {
		int ix = ofFontFamily.getSelectedIndex();
		String fname = (String)ofFontFamily.getChoice(ix);
		ix = ofFontSize.getSelectedIndex();
		int fs = ((Integer)ofFontSize.getChoice(ix)).intValue();
		FontFamily fam = null;		
		try {
			fam = FontFamily.forName(fname);
			Font f = fam.getFont(Font.PLAIN, fs, Ui.UNITS_pt);
			lfFontSample.setFont(f);
		} catch (ClassNotFoundException e) {
		}
		}
	}

	public void fieldChanged(Field field, int ixx) {
		focusChanged(field, ixx);
	}
	
	public Font getSelectedFont() {
		String fam = (String)ofFontFamily.getChoice(ofFontFamily.getSelectedIndex());
		String s = (String)ofFontSize.getChoice(ofFontFamily.getSelectedIndex());
		int size = Integer.parseInt(s);
		FontFamily ffam = null;
		try {
			ffam = FontFamily.forName(fam);
		} catch (ClassNotFoundException e) {
			return null;
		}
		Font f = ffam.getFont(Font.PLAIN, size, Ui.UNITS_pt);
		return (f);
	}
	
	public String getFamilyName() {
		return (String)ofFontFamily.getChoice(ofFontFamily.getSelectedIndex());		
	}

	public int getFontPointSize() {
		String s = (String)ofFontSize.getChoice(ofFontFamily.getSelectedIndex());
		return Integer.parseInt(s);
		
	}

}
