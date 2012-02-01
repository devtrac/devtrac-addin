package com.fairview5.keepassbb2.common.menu;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.SeparatorField;

public class KMenuSeparator extends SeparatorField implements KMenuItem {
	int color = -1;
	int backgroundColor = -1;

	public String getText() {
		return "";
	}

	public boolean isFontSet() {
		return false;
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
		if (color != -1) {
			graphics.setColor(color);
		}
		super.paint(graphics);
	}

	public boolean isStyleSet() {
		return false;
	}

	public void setStyle(int style) {
	}

	public void setBackgroundColor(int backgroundColor){
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
