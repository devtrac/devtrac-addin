package com.fairview5.keepassbb2.common.menu;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.LabelField;

public class KMenuAction extends LabelField implements KMenuItem, Runnable {
	char mnemonic;
	private int mnemonicPos;
	int color = -1;
	int backgroundColor = -1;
	boolean isFontSet = false;
	private boolean isStyleSet = false;
	private int style = KMenu.STYLE_LEFT;
	
	public KMenuAction(String label) {
		this(label, null, null, -1);
	}
	public KMenuAction(String label, String mnemonic, Object cookie) {
		this(label, cookie, null, -1);
	}
	public KMenuAction(String label, Object cookie, Font font, int color) {
		super(label+"  ", Field.FOCUSABLE | Field.USE_ALL_WIDTH);
		int ix = label.indexOf('\u0332');
		if (ix > 0) {
			mnemonicPos = ix-1;
			this.mnemonic = label.charAt(mnemonicPos);
		}
		this.setCookie(cookie);
		this.color = color;
		if (font != null) {
			setFont(font);
			isFontSet = true;
		}
	}
	
	public void run() {
	}
	
	public int getPreferredWidth() {
		return getFont().getAdvance(getText())+10;
	}
	public void paintBackground(Graphics graphics) {
		if (backgroundColor != -1) {
			graphics.setBackgroundColor(backgroundColor);
			graphics.clear();
		} else {
			super.paintBackground(graphics);
		}
	}
	
	public void paint(Graphics graphics) {
		XYRect xy = graphics.getClippingRect();
		String label = getText().trim();
		Font f = getFont();
		if (color != -1) graphics.setColor(color);
		int x = xy.x;
		int tw = f.getAdvance(getText());
		
		switch(style & KMenu.STYLE_HALIGN_MASK) {
		case KMenu.STYLE_LEFT:
			x = xy.x;
			break;
		case KMenu.STYLE_CENTER:
			x = (xy.width - tw) / 2;
			break;
		case KMenu.STYLE_RIGHT:
			x = xy.width - tw;
			break;
		default: 
			x = xy.x;
			break;
		}
		
		graphics.drawText(label, x, xy.y);
		return;
	}
	
	public boolean isFontSet() {
		return isFontSet;
	}
	public boolean isStyleSet() {
		return isStyleSet;
	}
	public void setStyle(int style) {
		this.style = style;
	}
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public int getBackgroundColor() {
		return backgroundColor;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public int getColor() {
		return color;
	}
	
}
