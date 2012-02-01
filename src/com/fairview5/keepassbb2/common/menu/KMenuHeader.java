package com.fairview5.keepassbb2.common.menu;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.LabelField;

public class KMenuHeader extends LabelField implements KMenuItem {
	private int color = -1;
	private int backgroundColor = -1;
	private boolean isFontSet = false;
	private boolean isStyleSet = false;
	private int style = KMenu.STYLE_LEFT;

	public KMenuHeader(String label) {
		super(label, Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH);
	}
	public KMenuHeader(String label, int style) {
		super(label, Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH);
		this.style = style;
	}
	public KMenuHeader(String label, int style, Font font, int color) {
		super(label, Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH);
		if (font != null) {
			setFont(font);
			isFontSet = true;
		}
		this.style = style;
		this.isStyleSet = true;
		this.setColor(color);
	}

	public int getPreferredWidth() {
		return getFont().getAdvance(getText())+20;
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
		//graphics.clear();
		XYRect xy = graphics.getClippingRect();
		String label = getText().trim();
		if (getColor() != -1)
			graphics.setColor(getColor());
		Font f = getFont();
		int tw = f.getAdvance(label);
		
		switch (style & KMenu.STYLE_HALIGN_MASK) {
		case KMenu.STYLE_LEFT:
			int x = graphics.drawText(label, xy.x, xy.y);
			if ((style & KMenu.STYLE_LINES) > 0) {
				graphics.drawLine(xy.x + x, xy.y + (xy.height / 2), xy.x + xy.width, xy.y + (xy.height / 2));
			}
			return;
		case KMenu.STYLE_CENTER:
			int tx = xy.x + ((xy.width - tw) / 2);
			if ((style & KMenu.STYLE_LINES) > 0)
				graphics.drawLine(xy.x, xy.y + (xy.height / 2), tx, xy.y + (xy.height / 2));
			graphics.drawText(label, tx, xy.y);
			if ((style & KMenu.STYLE_LINES) > 0)
				graphics.drawLine(tx + tw, xy.y + (xy.height / 2), xy.x + xy.width, xy.y + (xy.height / 2));
			return;
		case KMenu.STYLE_RIGHT:
			graphics.drawText(label, xy.width-tw, xy.y);
			if ((style & KMenu.STYLE_LINES) > 0) {
				graphics.drawLine(xy.x, xy.y + (xy.height / 2), xy.x + (xy.width-tw), xy.y + (xy.height / 2));
			}
			return;
		default:
			graphics.drawText(label, xy.x, xy.y);
			if ((style & KMenu.STYLE_LINES) > 0) {
				graphics.drawLine(xy.x + tw, xy.y + (xy.height / 2), xy.x + xy.width, xy.y + (xy.height / 2));
			}
			return;
		}
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
