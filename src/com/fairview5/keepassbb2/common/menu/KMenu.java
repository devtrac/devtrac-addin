package com.fairview5.keepassbb2.common.menu;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class KMenu extends PopupScreen {
	public static final int STYLE_HALIGN_MASK = 0x03;
	public static final int STYLE_LEFT = 0x00;
	public static final int STYLE_RIGHT = 0x01;
	public static final int STYLE_CENTER = 0x02;
	public static final int STYLE_LINES = 0x04;
	public static final int CANCELLED = -1;
	public static final int OK = 0;
	int rc = CANCELLED;
	static boolean isThemed = true;

	Screen target;
	int instance;
	Font actionFont;
	int actionStyle = STYLE_LEFT;
	Font headerFont;
	int headerStyle = STYLE_LEFT;

	public KMenu(int instance, Screen target) {
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR | Manager.USE_ALL_WIDTH) {
			public void paintBackground(Graphics graphics) {
				if (!isThemed)
					graphics.clear();
				else
					super.paintBackground(graphics);
			}
		});
		this.instance = instance;
		this.target = target;
	}

	public static void setThemed(boolean themed) {
		isThemed = themed;
	}

	public int doModal() {
		getUiEngine().pushModalScreen(this);
		return rc;
	}

	public void show() {
		UiApplication.getUiApplication().pushScreen(this);
	}

	public void addSelectionMenu() {
		final Field fwf = target.getFieldWithFocus();
		final Clipboard clip = Clipboard.getClipboard();
		if (fwf != null) {
			if (fwf.isSelectable()) {
				addItem(new KMenuSeparator());
				addItem(new KMenuAction(fwf.isSelecting() ? "Cancel S\u0332elect" : "S\u0332elect") {
					public void run() {
						fwf.select(!fwf.isSelecting());
					}
				});
			}
			if (fwf.isPasteable() && clip.get() != null) {
				addItem(new KMenuAction("P\u0332aste") {
					public void run() {
						fwf.paste(clip);
					}
				});
			}
			if (fwf.isSelectionCopyable()) {
				addItem(new KMenuAction("Copy\u0332") {
					public void run() {
						fwf.selectionCopy(clip);
						fwf.select(false);
					}
				});
			}
			if (fwf.isSelectionCutable()) {
				addItem(new KMenuAction("Cu\u0332t") {
					public void run() {
						fwf.selectionCut(clip);
						fwf.select(false);
					}
				});
			}
			if (fwf.isSelectionDeleteable()) {
				addItem(new KMenuAction("D\u0332elete") {
					public void run() {
						fwf.selectionDelete();
						fwf.select(false);
					}
				});
			}
		}
	}

	public void setActionFont(Font f) {
		this.actionFont = f;
	}

	public void setActionStyle(int style) {
		this.actionStyle = style;
	}

	public void setHeaderFont(Font f) {
		this.headerFont = f;
	}

	public void setHeaderStyle(int style) {
		this.headerStyle = style;
	}

	public void addItem(KMenuItem mi) {
		if (!mi.isFontSet()) {
			if (mi instanceof KMenuAction)
				mi.setFont(actionFont == null ? getFont() : actionFont);
			else if (mi instanceof KMenuHeader)
				mi.setFont(headerFont == null ? getFont() : headerFont);
		}
		if (!mi.isStyleSet()) {
			if (mi instanceof KMenuAction)
				mi.setStyle(actionStyle);
			else if (mi instanceof KMenuHeader)
				mi.setStyle(headerStyle);
		}
		add((Field) mi);
	}

	/*
	public void applyTheme() {
		if (isThemed)
			super.applyTheme();
	}
	*/

	protected void paintBackground(Graphics graphics) {
		if (isThemed) {
			super.paintBackground(graphics);
			return;
		}

		XYRect xy = graphics.getClippingRect();
		graphics.pushContext(xy, 0, 0);
		graphics.setGlobalAlpha(127);
		graphics.setColor(0xffffff);
		graphics.fillRect(xy.x, xy.y, xy.width - 10, 5);
		graphics.fillRect(xy.x, xy.y, 5, xy.height - 10);
		graphics.setColor(0x505050);
		graphics.drawRect(xy.x + 4, xy.y + 4, xy.width - 17, xy.height - 17);
		graphics.setGlobalAlpha(64);
		graphics.fillRect(xy.x + 10, xy.y + 10, xy.width - 17, xy.height - 17);
		graphics.popContext();
	}

	protected void sublayout(int width, int height) {
		int dh = Display.getHeight();
		int dw = Display.getWidth();
		super.sublayout(getPreferredWidth(), (int) (dh * 0.75));
		if (!isThemed) {
			XYRect mxy = getDelegate().getExtent();
			setExtent(mxy.width + 20, mxy.height + 20);
			setPositionChild(getDelegate(), 5, 5);
		}
		XYRect sxy = getExtent();
		if (instance == 1073741824) {
			setPosition(0, dh - sxy.height);
		} else if (instance == 65536 || instance == 65537) {
			setPosition((dw - sxy.width) / 2, dh - sxy.height);
		} else {
			setPosition(dw - sxy.width, 0);
		}

	}

	public int getPreferredWidth() {
		int w = 0;
		for (int i = 0; i < getFieldCount(); i++) {
			KMenuItem mi = ((KMenuItem) getField(i));
			w = Math.max(w, mi.getFont().getAdvance(mi.getText()) + 10);
		}
		return w + 40;
	}

	private void select() {
		Field f = getFieldWithFocus();
		if (!(f instanceof KMenuAction))
			return;
		KMenuAction kma = (KMenuAction) f;
		UiApplication.getUiApplication().invokeLater(kma);
		rc = OK;
	}

	protected boolean navigationClick(int status, int time) {
		select();
		close();
		return (true);
	}

	public void close() {
		deleteAll();
		super.close();
	}

	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			close();
			return (true);
		}
		if (c == Characters.ENTER) {
			select();
			close();
			return (true);
		}

		int ix = indexOfList(c, getFieldWithFocusIndex() + 1);
		if (ix >= 0) {
			getField(ix).setFocus();
			return true;
		} else {
			ix = indexOfList(c, 0);
			if (ix >= 0) {
				getField(ix).setFocus();
				return true;
			}
		}

		return super.keyChar(c, status, time);
	}

	public int indexOfList(char prefix, int start) {
		for (int i = start; i < getFieldCount(); i++) {
			KMenuItem mi = ((KMenuItem) getField(i));
			if (!(mi instanceof KMenuAction))
				continue;
			if (Character.toLowerCase(prefix) == Character.toLowerCase(((KMenuAction) mi).mnemonic)) {
				return i;
			}
		}
		return -1;
	}
}
