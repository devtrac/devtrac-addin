package com.fairview5.keepassbb2.common.menu;

import net.rim.device.api.ui.Font;


public interface KMenuItem {
	public String getText();
	public Font getFont();
	public boolean isFontSet();
	public void setFont(Font font);
	public boolean isStyleSet();
	public void setStyle(int style);
	public void setBackgroundColor(int backgroundColor);
	public int getBackgroundColor();
	public void setColor(int color);
	public int getColor();
}
